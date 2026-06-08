package com.jieqi.server.ws;

import com.google.gson.JsonObject;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import com.jieqi.core.RandomRevealService;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.json.JsonErrorCodes;
import com.jieqi.protocol.json.JsonMessageTypes;
import com.jieqi.protocol.json.JsonMessages;
import com.jieqi.protocol.json.PieceJsonMapper;
import com.jieqi.server.GameRecordStore;
import com.jieqi.server.MatchmakingService;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 老师 2026 大作业公共接口 — WebSocket + JSON 服务端。
 * 默认端口 8887（与老师示例一致），走子/翻子逻辑复用 jieqi-core。
 */
public class WsGameServer extends WebSocketServer {

    private static final long FIRST_HAND_WINDOW_MS = 10_000L;
    private static final long STEP_TIMEOUT_MS = Protocol.TIMEOUT_THRESHOLD;
    /** rematch 决定窗口：对局结束后保留 room 多久等待"再来一局"决定。 */
    private static final long REMATCH_WINDOW_MS = 10 * 60_000L;
    private static final int CHAT_MAX_LEN = 120;

    private final UserRegistry users = new UserRegistry();
    private final Map<WebSocket, WsPlayerContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocket> connByUserId = new ConcurrentHashMap<>();
    private final Map<String, WsRoom> rooms = new ConcurrentHashMap<>();
    private final Queue<String> matchQueue = new ArrayDeque<>();
    private final GameRecordStore recordStore = new GameRecordStore("records");
    private final RandomRevealService revealService = new RandomRevealService();
    // 真正的 AI：Alpha-Beta + 揭棋评估（含 SEE / KingHunt / 挂子惩罚等）。
    // RuleBasedBot 留作降级备用（若 JieqiAgent 抛出异常或超时返回 null）。
    private final com.jieqi.ai.JieqiAgent aiBot = new com.jieqi.ai.JieqiAgent();
    private final RuleBasedBot fallbackBot = new RuleBasedBot();
    // 时间预算：人机对战每步 5s（响应不卡），AI 自动对弈每步 2.5s（看着不无聊）
    // 评估器一次 evaluate 已优化为 2 次 generateAllMoves，5s 在中盘能搜到 4-5 层
    private static final long AI_BUDGET_HUMAN_VS_AI_MS = 5_000L;
    private static final long AI_BUDGET_AI_BATTLE_MS = 2_500L;
    private volatile boolean running = true;

    public WsGameServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        sessions.put(conn, new WsPlayerContext(conn));
        System.out.println("[WS] 新连接: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        WsPlayerContext ctx = sessions.remove(conn);
        if (ctx == null || !ctx.isLoggedIn()) {
            return;
        }
        connByUserId.remove(ctx.userId(), conn);
        matchQueue.remove(ctx.userId());
        handleDisconnect(ctx);
        System.out.println("[WS] 连接关闭: " + ctx.userId() + " — " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        WsPlayerContext ctx = sessions.get(conn);
        if (ctx == null) {
            return;
        }
        try {
            JsonObject json = JsonMessages.parse(message);
            String type = JsonMessages.messageType(json);
            switch (type) {
                case JsonMessageTypes.LOGIN -> handleLogin(ctx, json);
                case JsonMessageTypes.REGISTER -> handleRegister(ctx, json);
                case JsonMessageTypes.START_MATCH -> handleStartMatch(ctx);
                case JsonMessageTypes.START_AI_GAME -> handleStartAiGame(ctx);
                case JsonMessageTypes.START_AI_BATTLE -> handleStartAiBattle(ctx);
                case JsonMessageTypes.CANCEL_MATCH -> handleCancelMatch(ctx);
                case JsonMessageTypes.CREATE_ROOM -> handleCreateRoom(ctx);
                case JsonMessageTypes.JOIN_ROOM -> handleJoinRoom(ctx, json);
                case JsonMessageTypes.REQUEST_FIRST_HAND -> handleRequestFirstHand(ctx, json);
                case JsonMessageTypes.READY -> handleReady(ctx);
                case JsonMessageTypes.MOVE -> handleMove(ctx, json);
                case JsonMessageTypes.CHAT -> handleChat(ctx, json);
                case JsonMessageTypes.PING -> send(conn, JsonMessages.pong(
                        json.has("timestamp") ? json.get("timestamp").getAsLong() : System.currentTimeMillis()));
                case JsonMessageTypes.RESIGN -> handleResign(ctx);
                case JsonMessageTypes.DRAW_OFFER -> handleDrawOffer(ctx);
                case JsonMessageTypes.DRAW_ACCEPT -> handleDrawAccept(ctx);
                case JsonMessageTypes.DRAW_DECLINE -> handleDrawDecline(ctx);
                case JsonMessageTypes.REMATCH_REQUEST -> handleRematchRequest(ctx);
                case JsonMessageTypes.REMATCH_DECLINE -> handleRematchDecline(ctx);
                case JsonMessageTypes.LEAVE_ROOM -> handleLeaveRoom(ctx);
                case JsonMessageTypes.PAUSE_GAME -> handlePauseGame(ctx);
                case JsonMessageTypes.RESUME_GAME -> handleResumeGame(ctx);
                default -> send(conn, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "未知消息类型"));
            }
        } catch (Exception e) {
            send(conn, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "消息格式解析失败"));
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WS] 错误: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[WS] 揭棋 WebSocket 服务器已启动，端口: " + getPort());
        System.out.println("[WS] 协议: 老师公共接口 JSON (ws://localhost:" + getPort() + ")");
        System.out.println("[WS] 步时上限: " + STEP_TIMEOUT_MS + "ms");
        Thread timeoutThread = new Thread(this::timeoutLoop, "ws-timeout");
        timeoutThread.setDaemon(true);
        timeoutThread.start();
        Thread matchThread = new Thread(this::firstHandLoop, "ws-first-hand");
        matchThread.setDaemon(true);
        Thread rematchThread = new Thread(this::rematchCleanupLoop, "ws-rematch-cleanup");
        rematchThread.setDaemon(true);
        rematchThread.start();
        matchThread.start();
    }

    public void shutdown() {
        running = false;
        try {
            stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── 消息处理 ────────────────────────────────────────────

    private void handleLogin(WsPlayerContext ctx, JsonObject json) {
        String userId = json.has("userId") ? json.get("userId").getAsString() : null;
        String password = json.has("password") ? json.get("password").getAsString() : "";
        UserRegistry.UserAccount acc = users.loginOrCreate(userId, password);
        if (acc == null) {
            send(ctx, JsonMessages.loginResult(false, "登录失败", null));
            return;
        }
        WebSocket existing = connByUserId.put(acc.userId(), ctx.connection());
        if (existing != null && existing != ctx.connection()) {
            connByUserId.put(acc.userId(), existing);
            send(ctx, JsonMessages.error(JsonErrorCodes.DUPLICATE_LOGIN, "重复登录"));
            return;
        }
        ctx.setUser(acc.userId(), acc.nickname());
        send(ctx, JsonMessages.loginResult(true, "登录成功", acc.userId()));
    }

    private void handleRegister(WsPlayerContext ctx, JsonObject json) {
        String userId = json.get("userId").getAsString();
        String password = json.get("password").getAsString();
        String nickname = json.has("nickname") ? json.get("nickname").getAsString() : userId;
        if (!users.register(userId, password, nickname)) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "注册失败，账号已存在"));
            return;
        }
        send(ctx, JsonMessages.loginResult(true, "注册成功", userId));
        ctx.setUser(userId, nickname);
        connByUserId.put(userId, ctx.connection());
    }

    private void handleStartMatch(WsPlayerContext ctx) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        if (!prepareForNewRoom(ctx)) {
            return;
        }
        synchronized (this) {
            if (!matchQueue.contains(ctx.userId())) {
                matchQueue.offer(ctx.userId());
            }
            tryMatchLocked();
        }
    }

    private void handleCreateRoom(WsPlayerContext ctx) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        if (!prepareForNewRoom(ctx)) {
            return;
        }
        synchronized (this) {
            matchQueue.remove(ctx.userId());
            String roomId = generateRoomCode();
            Game game = new Game(roomId);
            WsRoom room = new WsRoom(roomId, game);
            rooms.put(roomId, room);
            room.bindPlayer(ctx, ChessPiece.RED);
            send(ctx, JsonMessages.matchSuccess(roomId, "", ""));
            System.out.println("[WS] 创建房间 roomId=" + roomId + " owner=" + ctx.userId());
        }
    }

    private void handleStartAiGame(WsPlayerContext ctx) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        if (!prepareForNewRoom(ctx)) {
            return;
        }
        synchronized (this) {
            matchQueue.remove(ctx.userId());
            String roomId = "ai_" + MatchmakingService.newGameId();
            Game game = new Game(roomId);
            WsRoom room = new WsRoom(roomId, game);
            rooms.put(roomId, room);
            room.bindPlayer(ctx, ChessPiece.RED);
            room.enableAiOpponent(ChessPiece.BLACK, "ai_bot", "规则托管 AI");
            send(ctx, JsonMessages.matchSuccess(roomId, room.aiUserId(), room.aiNickname()));
            System.out.println("[WS] 创建人机房间 roomId=" + roomId + " human=" + ctx.userId());
        }
    }

    private void handleStartAiBattle(WsPlayerContext ctx) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        if (!prepareForNewRoom(ctx)) {
            return;
        }
        synchronized (this) {
            matchQueue.remove(ctx.userId());
            String roomId = "ai_battle_" + MatchmakingService.newGameId();
            Game game = new Game(roomId);
            WsRoom room = new WsRoom(roomId, game);
            rooms.put(roomId, room);
            room.enableAiBattle(ctx, "ai_red", "规则 AI 红方", "ai_black", "规则 AI 黑方");
            send(ctx, JsonMessages.matchSuccess(roomId, "ai_black", "AI 自动对弈"));
            startAiBattleGame(room);
            System.out.println("[WS] 创建 AI 自动对弈 roomId=" + roomId + " viewer=" + ctx.userId());
        }
    }

    private void handleJoinRoom(WsPlayerContext ctx, JsonObject json) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        String roomId = json.has("roomId") ? json.get("roomId").getAsString().trim() : "";
        if (!roomId.matches("\\d{6}")) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "请输入 6 位房间号"));
            return;
        }
        if (!prepareForNewRoom(ctx)) {
            return;
        }
        synchronized (this) {
            matchQueue.remove(ctx.userId());
            WsRoom room = rooms.get(roomId);
            if (room == null || room.isStarted() || room.isFinished() || room.hasAiOpponent()) {
                send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "房间不存在或已开局"));
                return;
            }
            if (room.red() == null || room.black() != null) {
                send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "房间不可加入"));
                return;
            }
            if (room.red().userId().equals(ctx.userId())) {
                send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "不能加入自己的房间"));
                return;
            }
            room.bindPlayer(ctx, ChessPiece.BLACK);
            send(room.red(), JsonMessages.matchSuccess(roomId, ctx.userId(), ctx.nickname()));
            send(ctx, JsonMessages.matchSuccess(roomId, room.red().userId(), room.red().nickname()));
            System.out.println("[WS] 加入房间 roomId=" + roomId + " " + room.red().userId() + " vs " + ctx.userId());
        }
    }

    private boolean prepareForNewRoom(WsPlayerContext ctx) {
        if (ctx.roomId() == null) {
            return true;
        }
        WsRoom existing = rooms.get(ctx.roomId());
        if (existing != null && existing.isFinished()) {
            discardFinishedRoom(existing);
            return true;
        }
        send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "已在房间中"));
        return false;
    }

    private String generateRoomCode() {
        String roomId;
        do {
            roomId = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        } while (rooms.containsKey(roomId));
        return roomId;
    }

    private void handleCancelMatch(WsPlayerContext ctx) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        matchQueue.remove(ctx.userId());
        WsRoom room = roomOf(ctx);
        if (room == null) {
            return;
        }
        if (room.isStarted()) {
            return;
        }
        WsPlayerContext opp = room.opponentOf(ctx);
        if (opp != null) {
            send(opp, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "对手取消匹配"));
            resetMatchState(opp);
        }
        resetMatchState(ctx);
        rooms.remove(room.roomId());
    }

    private void resetMatchState(WsPlayerContext ctx) {
        ctx.setRoomId(null);
        ctx.setReady(false);
        ctx.setColor(-1);
        ctx.setWannaFirst(null);
    }

    private void handleRequestFirstHand(WsPlayerContext ctx, JsonObject json) {
        WsRoom room = roomOf(ctx);
        if (room == null || room.isStarted()) {
            return;
        }
        boolean wanna = json.has("wannaFirst") && json.get("wannaFirst").getAsBoolean();
        room.setWannaFirst(ctx.color(), wanna);
        tryStartGame(room);
    }

    private void handleReady(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || room.isStarted()) {
            return;
        }
        room.setReady(ctx.color(), true);
        ctx.setReady(true);
        WsPlayerContext opp = room.opponentOf(ctx);
        if (opp != null) {
            send(opp, JsonMessages.roomInfo(true));
        }
        tryStartGame(room);
    }

    private void handleMove(WsPlayerContext ctx, JsonObject json) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            send(ctx, JsonMessages.moveResult(false, null, false, null));
            return;
        }
        Game game = room.game();
        if (game.getStatus() != Game.GameStatus.PLAYING) {
            send(ctx, JsonMessages.moveResult(false, null, false, null));
            return;
        }
        if (room.isObserver(ctx)) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "AI 自动对弈中不能走子"));
            send(ctx, JsonMessages.moveResult(false, null, false, null));
            return;
        }
        if (game.getCurrentTurn() != ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.NOT_YOUR_TURN, "还没轮到你走棋"));
            send(ctx, JsonMessages.moveResult(false, null, false, null));
            return;
        }
        Move move;
        try {
            move = JsonMessages.parseMove(json);
        } catch (Exception e) {
            send(ctx, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "走法数据不完整"));
            return;
        }
        revealService.sanitizeClientMove(move);
        executeMoveAndBroadcast(room, move, ctx.color(), ctx);
    }

    private boolean executeMoveAndBroadcast(WsRoom room, Move move, int color, WsPlayerContext errorTarget) {
        synchronized (room) {
            Game game = room.game();
            String error = game.processMove(move, color);
            if (error != null) {
                if (errorTarget != null) {
                    send(errorTarget, JsonMessages.error(JsonErrorCodes.ILLEGAL_MOVE, error));
                    send(errorTarget, JsonMessages.moveResult(false, move, false, null));
                } else {
                    System.err.println("[WS] AI 非法走法 roomId=" + room.roomId() + ": " + error + " move=" + move);
                }
                return false;
            }
            revealService.stampServerRevealType(move, game.getBoard());
            room.clearDrawOffer();
            String flipResult = null;
            if (move.getType() != null) {
                flipResult = PieceJsonMapper.toJsonName(move.getType());
            }
            JsonObject result = JsonMessages.moveResult(true, move, true, flipResult);
            broadcastRoom(room, result);

            Game.GameStatus status = game.getStatus();
            if (status != Game.GameStatus.PLAYING) {
                broadcastGameOver(room, status);
            } else {
                scheduleAiMoveIfNeeded(room);
            }
            return true;
        }
    }

    private void scheduleAiMoveIfNeeded(WsRoom room) {
        Game game = room.game();
        if (room.isPaused()) {
            return;
        }
        if (game.getStatus() == Game.GameStatus.PLAYING && isAiTurn(room, game.getCurrentTurn())) {
            scheduleAiMove(room);
        }
    }

    private boolean isAiTurn(WsRoom room, int color) {
        return room.isAiBattle() || (room.hasAiOpponent() && room.aiColor() == color);
    }

    private void scheduleAiMove(WsRoom room) {
        Thread aiThread = new Thread(() -> {
            // 调度延迟不参与搜索质量，只控制观感节奏；人机保留极短缓冲，避免交互和 AI 搜索争锁。
            long preDelayMs = room.isAiBattle() ? 150L : 100L;
            try {
                Thread.sleep(preDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            synchronized (room) {
                Game game = room.game();
                if (!room.isStarted()
                        || room.isPaused()
                        || !isAiTurn(room, game.getCurrentTurn())
                        || game.getStatus() != Game.GameStatus.PLAYING
                        || !rooms.containsKey(room.roomId())) {
                    return;
                }
                int aiColor = game.getCurrentTurn();
                long budget = room.isAiBattle() ? AI_BUDGET_AI_BATTLE_MS : AI_BUDGET_HUMAN_VS_AI_MS;
                Move aiMove = null;
                try {
                    aiMove = aiBot.selectMove(game.getBoard(), aiColor, budget);
                } catch (Exception ex) {
                    System.err.println("[WS] JieqiAgent 异常，降级到 RuleBasedBot: " + ex);
                }
                if (aiMove == null) {
                    aiMove = fallbackBot.selectMove(game.getBoard(), aiColor);
                }
                if (aiMove == null) {
                    int winnerColor = aiColor == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED;
                    game.setStatus(winnerColor == ChessPiece.RED
                            ? Game.GameStatus.RED_WIN : Game.GameStatus.BLACK_WIN);
                    game.setGameOverReason(Protocol.REASON_CHECKMATE);
                    broadcastGameOver(room, game.getStatus());
                    return;
                }
                revealService.sanitizeClientMove(aiMove);
                executeMoveAndBroadcast(room, aiMove, aiColor, null);
            }
        }, "ws-ai-move-" + room.roomId());
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void handleChat(WsPlayerContext ctx, JsonObject json) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先登录"));
            return;
        }
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前不在对局中"));
            return;
        }
        if (room.hasAiOpponent() || room.isAiBattle()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "仅真人对局支持聊天"));
            return;
        }
        if (room.game().getStatus() != Game.GameStatus.PLAYING) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "对局已结束"));
            return;
        }
        String content = sanitizeChatContent(json.has("content") ? json.get("content").getAsString() : "");
        if (content.isEmpty()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "聊天内容不能为空"));
            return;
        }
        String fromColor = PieceJsonMapper.colorToString(ctx.color());
        broadcastRoom(room, JsonMessages.chatMessage(ctx.userId(), fromColor, content, System.currentTimeMillis()));
    }

    private String sanitizeChatContent(String raw) {
        String content = raw == null ? "" : raw.replace('\n', ' ').replace('\r', ' ').trim();
        if (content.length() > CHAT_MAX_LEN) {
            return content.substring(0, CHAT_MAX_LEN);
        }
        return content;
    }

    private void handleResign(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            return;
        }
        Game game = room.game();
        if (game.getStatus() != Game.GameStatus.PLAYING) {
            return;
        }
        if (room.isObserver(ctx)) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "AI 自动对弈中不能认输"));
            return;
        }
        int winnerColor = ctx.isRed() ? ChessPiece.BLACK : ChessPiece.RED;
        game.setStatus(winnerColor == ChessPiece.RED ? Game.GameStatus.RED_WIN : Game.GameStatus.BLACK_WIN);
        game.setGameOverReason(Protocol.REASON_RESIGN);
        broadcastGameOver(room, game.getStatus());
    }

    private void handleDrawOffer(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前不在对局中"));
            return;
        }
        Game game = room.game();
        if (game.getStatus() != Game.GameStatus.PLAYING) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "对局已结束"));
            return;
        }
        if (room.hasAiOpponent() || room.isAiBattle()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "AI 对局不支持提和"));
            return;
        }
        int offeredBy = room.drawOfferedByColor();
        if (offeredBy == ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "已发起提和，请等待对方回应"));
            return;
        }
        if (offeredBy != -1) {
            handleDrawAccept(ctx);
            return;
        }
        room.setDrawOfferedByColor(ctx.color());
        broadcastRoom(room, JsonMessages.drawOffered(ctx.userId()));
    }

    private void handleDrawAccept(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前不在对局中"));
            return;
        }
        Game game = room.game();
        if (game.getStatus() != Game.GameStatus.PLAYING) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "对局已结束"));
            return;
        }
        int offeredBy = room.drawOfferedByColor();
        if (offeredBy == -1) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前没有待处理的提和"));
            return;
        }
        if (offeredBy == ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "不能接受自己发起的提和"));
            return;
        }
        room.clearDrawOffer();
        game.setStatus(Game.GameStatus.DRAW);
        game.setGameOverReason(Protocol.REASON_AGREED_DRAW);
        broadcastGameOver(room, game.getStatus());
    }

    private void handleDrawDecline(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            return;
        }
        int offeredBy = room.drawOfferedByColor();
        if (offeredBy == -1 || offeredBy == ctx.color()) {
            return;
        }
        room.clearDrawOffer();
        WsPlayerContext opp = room.opponentOf(ctx);
        if (opp != null) {
            send(opp, JsonMessages.drawDeclined(ctx.userId()));
        }
    }

    private void handleDisconnect(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null) {
            return;
        }
        Game game = room.game();
        if (game.getStatus() == Game.GameStatus.PLAYING) {
            if (room.isObserver(ctx)) {
                game.setStatus(Game.GameStatus.DRAW);
                rooms.remove(room.roomId());
                ctx.setRoomId(null);
                return;
            }
            game.disconnectPlayer(ctx.color());
            game.setGameOverReason(Protocol.REASON_DISCONNECT);
            broadcastGameOver(room, game.getStatus());
        } else if (!room.isStarted() && !room.isFinished()) {
            WsPlayerContext opp = room.opponentOf(ctx);
            if (opp != null) {
                send(opp, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "对手离开房间"));
                resetMatchState(opp);
            }
            rooms.remove(room.roomId());
        }
        ctx.setRoomId(null);
        ctx.setReady(false);
    }

    // ── 匹配与开局 ────────────────────────────────────────────

    private synchronized void tryMatchLocked() {
        while (matchQueue.size() >= 2) {
            String u1 = matchQueue.poll();
            String u2 = matchQueue.poll();
            if (u1 == null || u2 == null) {
                break;
            }
            WebSocket c1 = connByUserId.get(u1);
            WebSocket c2 = connByUserId.get(u2);
            if (c1 == null || c2 == null) {
                if (c1 != null) {
                    matchQueue.offer(u1);
                }
                if (c2 != null) {
                    matchQueue.offer(u2);
                }
                continue;
            }
            WsPlayerContext p1 = sessions.get(c1);
            WsPlayerContext p2 = sessions.get(c2);
            if (p1 == null || p2 == null) {
                continue;
            }
            String roomId = "room_" + MatchmakingService.newGameId();
            Game game = new Game(roomId);
            WsRoom room = new WsRoom(roomId, game);
            rooms.put(roomId, room);
            room.bindPlayer(p1, ChessPiece.RED);
            room.bindPlayer(p2, ChessPiece.BLACK);
            send(p1, JsonMessages.matchSuccess(roomId, u2, users.nickname(u2)));
            send(p2, JsonMessages.matchSuccess(roomId, u1, users.nickname(u1)));
            System.out.println("[WS] 匹配成功 roomId=" + roomId + " " + u1 + " vs " + u2);
        }
    }

    private synchronized void tryStartGame(WsRoom room) {
        if (room.isStarted() || !room.bothReady()) {
            return;
        }
        assignColorsByFirstHand(room);
        room.setStarted(true);
        Game game = room.game();
        game.setStatus(Game.GameStatus.PLAYING);
        game.setTurnStartTime(System.currentTimeMillis());
        broadcastGameStart(room);
        System.out.println("[WS] 开局 roomId=" + room.roomId());
        scheduleAiMoveIfNeeded(room);
    }

    private void assignColorsByFirstHand(WsRoom room) {
        if (room.hasAiOpponent() || room.isAiBattle()) {
            return;
        }
        Boolean r = room.redWannaFirst();
        Boolean b = room.blackWannaFirst();
        if (r == null) {
            r = false;
        }
        if (b == null) {
            b = false;
        }
        if (r && b) {
            if (Math.random() < 0.5) {
                room.swapColors();
            }
        } else if (b && !r) {
            room.swapColors();
        }
    }

    private void broadcastGameStart(WsRoom room) {
        Game game = room.game();
        String redId = userIdForColor(room, ChessPiece.RED);
        String blackId = userIdForColor(room, ChessPiece.BLACK);
        if (room.red() != null) {
            send(room.red(), JsonMessages.gameStart(
                    redId, blackId, "red", true, game.getBoard()));
        }
        if (room.black() != null) {
            send(room.black(), JsonMessages.gameStart(
                    redId, blackId, "black", false, game.getBoard()));
        }
        if (room.observer() != null) {
            send(room.observer(), JsonMessages.gameStart(
                    redId, blackId, "red", false, game.getBoard()));
        }
    }

    private void startAiBattleGame(WsRoom room) {
        room.setStarted(true);
        Game game = room.game();
        game.setStatus(Game.GameStatus.PLAYING);
        game.setTurnStartTime(System.currentTimeMillis());
        broadcastGameStart(room);
        scheduleAiMoveIfNeeded(room);
    }

    private void firstHandLoop() {
        while (running) {
            try {
                Thread.sleep(500);
                for (WsRoom room : rooms.values()) {
                    if (!room.isStarted() && room.bothReady()) {
                        long elapsed = System.currentTimeMillis() - room.matchedAtMs();
                        if (elapsed >= FIRST_HAND_WINDOW_MS) {
                            tryStartGame(room);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ── 超时与终局 ────────────────────────────────────────────

    private void timeoutLoop() {
        while (running) {
            try {
                Thread.sleep(1000);
                for (WsRoom room : rooms.values()) {
                    Game game = room.game();
                    if (game.getStatus() != Game.GameStatus.PLAYING || !room.isStarted()) {
                        continue;
                    }
                    if (!game.isTimeout()) {
                        continue;
                    }
                    int timeoutColor = game.getCurrentTurn();
                    int winnerColor = timeoutColor == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED;
                    game.setStatus(winnerColor == ChessPiece.RED
                            ? Game.GameStatus.RED_WIN : Game.GameStatus.BLACK_WIN);
                    game.setGameOverReason(Protocol.REASON_TIMEOUT);
                    String loserId = userIdForColor(room, timeoutColor);
                    String winnerId = userIdForColor(room, winnerColor);
                    broadcastRoom(room, JsonMessages.timeout(loserId, winnerId));
                    broadcastGameOver(room, game.getStatus());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void broadcastGameOver(WsRoom room, Game.GameStatus status) {
        int winnerColor = JsonMessages.winnerColorFromStatus(status);
        String winnerStr = winnerColor == ChessPiece.RED ? "red"
                : winnerColor == ChessPiece.BLACK ? "black" : "draw";
        String winnerId = winnerColor == ChessPiece.RED || winnerColor == ChessPiece.BLACK
                ? userIdForColor(room, winnerColor) : "";
        int reasonCode = room.game().getGameOverReason();
        if (reasonCode < 0) {
            reasonCode = Protocol.REASON_CHECKMATE;
        }
        String reason = JsonMessages.reasonFromProtocolCode(reasonCode);
        broadcastRoom(room, JsonMessages.gameOver(winnerStr, reason, winnerId));
        persistRecord(room.game());
        // 不立即销毁 room，标记 finished：保留 REMATCH_WINDOW_MS 等待双方决定是否再来一局。
        // 超时由 rematchCleanupLoop 回收。
        room.markFinished();
        System.out.println("[WS] 对局结束 roomId=" + room.roomId() + " " + status
                + "（等待 rematch 决定，" + REMATCH_WINDOW_MS / 1000 + "s 后自动清理）");
    }

    /** 销毁一个"已结束等待 rematch"的 room，并把双方玩家踢回大厅。 */
    private void discardFinishedRoom(WsRoom room) {
        rooms.remove(room.roomId());
        if (room.red() != null && room.roomId().equals(room.red().roomId())) {
            room.red().setRoomId(null);
        }
        if (room.black() != null && room.roomId().equals(room.black().roomId())) {
            room.black().setRoomId(null);
        }
        if (room.observer() != null && room.roomId().equals(room.observer().roomId())) {
            room.observer().setRoomId(null);
        }
        System.out.println("[WS] 清理对局房间 roomId=" + room.roomId());
    }

    // ── Rematch（本组扩展） ─────────────────────────────────────

    private void handleRematchRequest(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isFinished()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前不在已结束的对局房间中"));
            return;
        }
        if (room.hasAiOpponent()) {
            startRematchGame(room);
            return;
        }
        if (room.isAiBattle()) {
            startRematchGame(room);
            return;
        }
        room.setRematchAsked(ctx.color(), true);
        if (room.bothRematchAsked()) {
            // 双方都同意 → 开新一局，沿用原 roomId 和双方
            startRematchGame(room);
        } else {
            // 仅一方请求 → 通知对方
            WsPlayerContext opp = room.opponentOf(ctx);
            if (opp != null) {
                send(opp, JsonMessages.rematchOffer(ctx.userId()));
            }
        }
    }

    private void handleRematchDecline(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isFinished()) return;
        WsPlayerContext opp = room.opponentOf(ctx);
        if (opp != null) {
            send(opp, JsonMessages.rematchDeclined(ctx.userId()));
        }
        discardFinishedRoom(room);
    }

    /**
     * 主动离开房间：
     *  - AI 对战观战 / 人机对弈：直接 discard 房间，回到大厅
     *  - 真人对局：当作 resign 处理
     *  - 已结束的房间：直接清理
     */
    private void handleLeaveRoom(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null) return;
        if (room.isFinished()) {
            discardFinishedRoom(room);
            return;
        }
        if (room.isAiBattle() || room.hasAiOpponent()) {
            // AI 模式：没有对手要通知，直接销毁房间
            rooms.remove(room.roomId());
            if (room.red() != null) room.red().setRoomId(null);
            if (room.black() != null) room.black().setRoomId(null);
            if (room.observer() != null) room.observer().setRoomId(null);
            System.out.println("[WS] 用户主动离开 AI 房间 roomId=" + room.roomId()
                    + " user=" + ctx.userId());
            return;
        }
        // 真人对局中途离开 = 认输
        if (room.isStarted() && room.game().getStatus() == Game.GameStatus.PLAYING) {
            handleResign(ctx);
        } else {
            // 真人房间但还没开局，按取消匹配处理
            handleCancelMatch(ctx);
        }
    }

    /** 暂停 AI 对弈（仅 AI 对战 / 人机模式）。 */
    private void handlePauseGame(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) return;
        if (!(room.isAiBattle() || room.hasAiOpponent())) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "仅人机对战或 AI 自动对弈可暂停"));
            return;
        }
        if (room.isPaused()) return;
        room.setPaused(true);
        broadcastRoom(room, JsonMessages.gamePaused());
        System.out.println("[WS] AI 对弈已暂停 roomId=" + room.roomId());
    }

    /** 恢复 AI 对弈。 */
    private void handleResumeGame(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) return;
        if (!(room.isAiBattle() || room.hasAiOpponent())) return;
        if (!room.isPaused()) return;
        room.setPaused(false);
        broadcastRoom(room, JsonMessages.gameResumed());
        System.out.println("[WS] AI 对弈已恢复 roomId=" + room.roomId());
        // 主动触发当前回合的 AI 走子（若该 AI 走）
        scheduleAiMoveIfNeeded(room);
    }

    /** 双方同意后，在同一 room 上启动新一局 Game 并广播 gameStart。 */
    private void startRematchGame(WsRoom room) {
        // 用新的 Game 实例（重新随机暗子分配 + 重置回合）
        Game newGame = new Game(room.roomId());
        room.replaceGame(newGame);
        // 把双方玩家重新接入 Game（保留原颜色）
        if (room.red() != null) {
            newGame.setRedPlayerName(room.red().nickname());
            newGame.connectPlayer(ChessPiece.RED);
        }
        if (room.black() != null) {
            newGame.setBlackPlayerName(room.black().nickname());
            newGame.connectPlayer(ChessPiece.BLACK);
        }
        if (room.hasAiOpponent()) {
            newGame.setBlackPlayerName(room.aiNickname());
            newGame.connectPlayer(room.aiColor());
        }
        if (room.isAiBattle()) {
            newGame.setRedPlayerName(room.aiNicknameForColor(ChessPiece.RED));
            newGame.setBlackPlayerName(room.aiNicknameForColor(ChessPiece.BLACK));
            newGame.connectPlayer(ChessPiece.RED);
            newGame.connectPlayer(ChessPiece.BLACK);
        }
        newGame.setStatus(Game.GameStatus.PLAYING);
        room.resetForRematch();
        room.setStarted(true);

        if (room.red() != null) {
            send(room.red(), JsonMessages.gameStart(
                    userIdForColor(room, ChessPiece.RED), userIdForColor(room, ChessPiece.BLACK),
                    "red", true, newGame.getBoard()));
        }
        if (room.black() != null) {
            send(room.black(), JsonMessages.gameStart(
                    userIdForColor(room, ChessPiece.RED), userIdForColor(room, ChessPiece.BLACK),
                    "black", false, newGame.getBoard()));
        }
        if (room.observer() != null) {
            send(room.observer(), JsonMessages.gameStart(
                    userIdForColor(room, ChessPiece.RED), userIdForColor(room, ChessPiece.BLACK),
                    "red", false, newGame.getBoard()));
        }
        System.out.println("[WS] Rematch 开局 roomId=" + room.roomId());
        scheduleAiMoveIfNeeded(room);
    }

    /** 超时清理：超过 rematch 窗口后仍在 finished 状态的 room 自动销毁。 */
    private void rematchCleanupLoop() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            long now = System.currentTimeMillis();
            for (WsRoom room : new java.util.ArrayList<>(rooms.values())) {
                if (room.isFinished() && now - room.finishedAtMs() > REMATCH_WINDOW_MS) {
                    discardFinishedRoom(room);
                }
            }
        }
    }

    private void persistRecord(Game game) {
        if (game.getRecord().getLines().isEmpty()) {
            return;
        }
        try {
            Path path = recordStore.save(game);
            System.out.println("[WS] 棋谱已保存: " + path);
        } catch (IOException e) {
            System.err.println("[WS] 棋谱保存失败: " + e.getMessage());
        }
    }

    // ── 工具 ────────────────────────────────────────────────

    private WsRoom roomOf(WsPlayerContext ctx) {
        if (ctx.roomId() == null) {
            return null;
        }
        return rooms.get(ctx.roomId());
    }

    private void send(WebSocket conn, JsonObject msg) {
        if (conn != null && conn.isOpen()) {
            conn.send(JsonMessages.toJson(msg));
        }
    }

    private void send(WsPlayerContext ctx, JsonObject msg) {
        if (ctx == null) {
            return;
        }
        send(ctx.connection(), msg);
    }

    private void broadcastRoom(WsRoom room, JsonObject msg) {
        send(room.red(), msg);
        send(room.black(), msg);
        send(room.observer(), msg);
    }

    private String userIdForColor(WsRoom room, int color) {
        if (color == ChessPiece.RED) {
            if (room.red() != null) {
                return room.red().userId();
            }
        } else {
            if (room.black() != null) {
                return room.black().userId();
            }
        }
        String aiUserId = room.aiUserIdForColor(color);
        return aiUserId == null ? "" : aiUserId;
    }

    /** 集成测试：使指定房间当前回合步时过期。 */
    void expireTurnForTest(String roomId) {
        WsRoom room = rooms.get(roomId);
        if (room != null && room.isStarted()) {
            room.game().setTurnStartTime(System.currentTimeMillis() - 70_000L);
        }
    }

    /** 集成测试：房间是否仍存在。 */
    boolean hasRoomForTest(String roomId) {
        return rooms.containsKey(roomId);
    }

    /** 集成测试：房间是否处于"对局已结束、等待 rematch"状态。 */
    boolean isFinishedRoomForTest(String roomId) {
        WsRoom r = rooms.get(roomId);
        return r != null && r.isFinished();
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8887;
        WsGameServer server = new WsGameServer(port);
        server.start();
    }
}

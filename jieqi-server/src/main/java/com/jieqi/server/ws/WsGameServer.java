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

    private final UserRegistry users = new UserRegistry();
    private final Map<WebSocket, WsPlayerContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocket> connByUserId = new ConcurrentHashMap<>();
    private final Map<String, WsRoom> rooms = new ConcurrentHashMap<>();
    private final Queue<String> matchQueue = new ArrayDeque<>();
    private final GameRecordStore recordStore = new GameRecordStore("records");
    private final RandomRevealService revealService = new RandomRevealService();
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
                case JsonMessageTypes.CANCEL_MATCH -> handleCancelMatch(ctx);
                case JsonMessageTypes.CREATE_ROOM -> handleCreateRoom(ctx);
                case JsonMessageTypes.JOIN_ROOM -> handleJoinRoom(ctx, json);
                case JsonMessageTypes.REQUEST_FIRST_HAND -> handleRequestFirstHand(ctx, json);
                case JsonMessageTypes.READY -> handleReady(ctx);
                case JsonMessageTypes.MOVE -> handleMove(ctx, json);
                case JsonMessageTypes.PING -> send(conn, JsonMessages.pong(
                        json.has("timestamp") ? json.get("timestamp").getAsLong() : System.currentTimeMillis()));
                case JsonMessageTypes.RESIGN -> handleResign(ctx);
                case JsonMessageTypes.DRAW_OFFER -> handleDrawOffer(ctx);
                case JsonMessageTypes.DRAW_ACCEPT -> handleDrawAccept(ctx);
                case JsonMessageTypes.DRAW_DECLINE -> handleDrawDecline(ctx);
                case JsonMessageTypes.UNDO_OFFER -> handleUndoOffer(ctx);
                case JsonMessageTypes.UNDO_ACCEPT -> handleUndoAccept(ctx);
                case JsonMessageTypes.UNDO_DECLINE -> handleUndoDecline(ctx);
                case JsonMessageTypes.REMATCH_REQUEST -> handleRematchRequest(ctx);
                case JsonMessageTypes.REMATCH_DECLINE -> handleRematchDecline(ctx);
                default -> send(conn, JsonMessages.error(JsonErrorCodes.JSON_FORMAT,
                        "未知 messageType: " + type));
            }
        } catch (Exception e) {
            send(conn, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "JSON 解析失败: " + e.getMessage()));
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
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先 Login"));
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
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先 Login"));
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

    private void handleJoinRoom(WsPlayerContext ctx, JsonObject json) {
        if (!ctx.isLoggedIn()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先 Login"));
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
            if (room == null || room.isStarted() || room.isFinished()) {
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
            send(ctx, JsonMessages.error(JsonErrorCodes.LOGIN_FAILED, "请先 Login"));
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
        if (game.getCurrentTurn() != ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.NOT_YOUR_TURN, "未轮到本方走子"));
            send(ctx, JsonMessages.moveResult(false, null, false, null));
            return;
        }
        Move move;
        try {
            move = JsonMessages.parseMove(json);
        } catch (Exception e) {
            send(ctx, JsonMessages.error(JsonErrorCodes.JSON_FORMAT, "move 字段不完整"));
            return;
        }
        revealService.sanitizeClientMove(move);
        String error = game.processMove(move, ctx.color());
        if (error != null) {
            send(ctx, JsonMessages.error(JsonErrorCodes.ILLEGAL_MOVE, error));
            send(ctx, JsonMessages.moveResult(false, move, false, null));
            return;
        }
        revealService.stampServerRevealType(move, game.getBoard());
        room.clearDrawOffer();
        room.clearUndoOffer();
        String flipResult = null;
        if (move.getType() != null) {
            flipResult = PieceJsonMapper.toJsonName(move.getType());
        }
        JsonObject result = JsonMessages.moveResult(true, move, true, flipResult);
        broadcastRoom(room, result);

        Game.GameStatus status = game.getStatus();
        if (status != Game.GameStatus.PLAYING) {
            broadcastGameOver(room, status);
        }
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

    private void handleUndoOffer(WsPlayerContext ctx) {
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
        if (!game.canUndoLastMove()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前没有可悔棋的走法"));
            return;
        }
        int offeredBy = room.undoOfferedByColor();
        if (offeredBy == ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "已发起悔棋，请等待对方回应"));
            return;
        }
        if (offeredBy != -1) {
            handleUndoAccept(ctx);
            return;
        }
        room.setUndoOfferedByColor(ctx.color());
        broadcastRoom(room, JsonMessages.undoOffered(ctx.userId()));
    }

    private void handleUndoAccept(WsPlayerContext ctx) {
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
        int offeredBy = room.undoOfferedByColor();
        if (offeredBy == -1) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前没有待处理的悔棋"));
            return;
        }
        if (offeredBy == ctx.color()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "不能同意自己发起的悔棋"));
            return;
        }
        room.clearUndoOffer();
        room.clearDrawOffer();
        if (!game.undoLastMove()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前没有可悔棋的走法"));
            return;
        }
        String currentTurn = game.getCurrentTurn() == ChessPiece.RED ? "red" : "black";
        broadcastRoom(room, JsonMessages.undoPerformed(game.getBoard(), currentTurn));
    }

    private void handleUndoDecline(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isStarted()) {
            return;
        }
        int offeredBy = room.undoOfferedByColor();
        if (offeredBy == -1 || offeredBy == ctx.color()) {
            return;
        }
        room.clearUndoOffer();
        WsPlayerContext opp = room.opponentOf(ctx);
        if (opp != null) {
            send(opp, JsonMessages.undoDeclined(ctx.userId()));
        }
    }

    private void handleDisconnect(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null) {
            return;
        }
        Game game = room.game();
        if (game.getStatus() == Game.GameStatus.PLAYING) {
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
    }

    private void assignColorsByFirstHand(WsRoom room) {
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
        send(room.red(), JsonMessages.gameStart(
                room.red().userId(), room.black().userId(),
                "red", true, game.getBoard()));
        send(room.black(), JsonMessages.gameStart(
                room.red().userId(), room.black().userId(),
                "black", false, game.getBoard()));
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
                    String loserId = timeoutColor == ChessPiece.RED
                            ? room.red().userId() : room.black().userId();
                    String winnerId = winnerColor == ChessPiece.RED
                            ? room.red().userId() : room.black().userId();
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
        String winnerId = winnerColor == ChessPiece.RED ? room.red().userId()
                : winnerColor == ChessPiece.BLACK ? room.black().userId() : "";
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
        System.out.println("[WS] 清理对局房间 roomId=" + room.roomId());
    }

    // ── Rematch（本组扩展） ─────────────────────────────────────

    private void handleRematchRequest(WsPlayerContext ctx) {
        WsRoom room = roomOf(ctx);
        if (room == null || !room.isFinished()) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "当前不在已结束的对局房间中"));
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
        newGame.setStatus(Game.GameStatus.PLAYING);
        room.resetForRematch();
        room.setStarted(true);

        if (room.red() != null) {
            send(room.red(), JsonMessages.gameStart(
                    room.red().userId(), room.black() != null ? room.black().userId() : "",
                    "red", true, newGame.getBoard()));
        }
        if (room.black() != null) {
            send(room.black(), JsonMessages.gameStart(
                    room.red() != null ? room.red().userId() : "", room.black().userId(),
                    "black", false, newGame.getBoard()));
        }
        System.out.println("[WS] Rematch 开局 roomId=" + room.roomId());
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
        send(ctx.connection(), msg);
    }

    private void broadcastRoom(WsRoom room, JsonObject msg) {
        send(room.red(), msg);
        send(room.black(), msg);
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

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

/**
 * 老师 2026 大作业公共接口 — WebSocket + JSON 服务端。
 * 默认端口 8887（与老师示例一致），走子/翻子逻辑复用 jieqi-core。
 */
public class WsGameServer extends WebSocketServer {

    private static final long FIRST_HAND_WINDOW_MS = 10_000L;
    private static final long STEP_TIMEOUT_MS = Protocol.TIMEOUT_THRESHOLD;

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
                case JsonMessageTypes.REQUEST_FIRST_HAND -> handleRequestFirstHand(ctx, json);
                case JsonMessageTypes.READY -> handleReady(ctx);
                case JsonMessageTypes.MOVE -> handleMove(ctx, json);
                case JsonMessageTypes.PING -> send(conn, JsonMessages.pong(
                        json.has("timestamp") ? json.get("timestamp").getAsLong() : System.currentTimeMillis()));
                case JsonMessageTypes.RESIGN -> handleResign(ctx);
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
        if (ctx.roomId() != null) {
            send(ctx, JsonMessages.error(JsonErrorCodes.MATCH_FAILED, "已在房间中"));
            return;
        }
        synchronized (this) {
            if (!matchQueue.contains(ctx.userId())) {
                matchQueue.offer(ctx.userId());
            }
            tryMatchLocked();
        }
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
        rooms.remove(room.roomId());
        // 复位双方玩家的 roomId，否则下一局 match 会因 ctx.roomId() != null 被拒（3002 已在房间中）
        if (room.red() != null) room.red().setRoomId(null);
        if (room.black() != null) room.black().setRoomId(null);
        System.out.println("[WS] 对局结束 roomId=" + room.roomId() + " " + status);
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

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8887;
        WsGameServer server = new WsGameServer(port);
        server.start();
    }
}

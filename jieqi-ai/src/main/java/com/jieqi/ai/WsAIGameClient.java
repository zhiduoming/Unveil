package com.jieqi.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.json.BoardJsonMapper;
import com.jieqi.protocol.json.JsonMessageTypes;
import com.jieqi.protocol.json.JsonMessages;
import com.jieqi.protocol.json.PieceJsonMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket JSON 自动对弈 AI 客户端（老师公共接口）。
 */
public class WsAIGameClient extends WebSocketClient {

    private final JieqiAgent agent = new JieqiAgent();
    private final Board board = new Board();
    private final String userId;
    private final String password;

    private int myColor = -1;
    private boolean inGame;
    private volatile boolean running = true;
    private final CountDownLatch loginLatch = new CountDownLatch(1);

    public WsAIGameClient(URI serverUri, String userId, String password) {
        super(serverUri);
        this.userId = userId;
        this.password = password;
    }

    public void startAutoPlay() throws Exception {
        System.out.println("[WsAI] 连接 " + getURI() + " 用户=" + userId);
        connectBlocking(10, TimeUnit.SECONDS);
        sendJson(loginMessage());
        if (!loginLatch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("登录超时");
        }
        while (running && isOpen() && inGame) {
            Thread.sleep(200);
        }
        if (isOpen()) {
            close();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WsAI] 已连接服务器");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonMessages.parse(message);
            String type = JsonMessages.messageType(json);
            switch (type) {
                case JsonMessageTypes.LOGIN_RESULT -> handleLoginResult(json);
                case JsonMessageTypes.MATCH_SUCCESS -> {
                    System.out.println("[WsAI] 匹配成功 room=" + json.get("roomId").getAsString()
                            + " 对手=" + json.get("opponentNickname").getAsString());
                    sendJson(readyMessage());
                }
                case JsonMessageTypes.GAME_START -> handleGameStart(json);
                case JsonMessageTypes.MOVE_RESULT -> handleMoveResult(json);
                case JsonMessageTypes.TIMEOUT -> {
                    System.out.println("[WsAI] 超时: " + message);
                    running = false;
                }
                case JsonMessageTypes.GAME_OVER -> {
                    System.out.println("[WsAI] 对局结束: " + message);
                    inGame = false;
                    running = false;
                }
                case JsonMessageTypes.ERROR -> System.out.println("[WsAI] 错误 "
                        + json.get("code").getAsInt() + ": " + json.get("message").getAsString());
                default -> { }
            }
        } catch (Exception e) {
            System.err.println("[WsAI] 解析消息失败: " + e.getMessage());
        }
    }

    private void handleLoginResult(JsonObject json) {
        if (json.get("success").getAsBoolean()) {
            System.out.println("[WsAI] 登录成功");
            sendJson(startMatchMessage());
        } else {
            System.err.println("[WsAI] 登录失败: " + json.get("message").getAsString());
            running = false;
        }
        loginLatch.countDown();
    }

    private void handleGameStart(JsonObject json) {
        myColor = PieceJsonMapper.colorFromString(json.get("yourColor").getAsString());
        inGame = true;
        JsonArray cells = json.getAsJsonArray("initialBoard");
        BoardJsonMapper.applyInitialBoard(board, cells);
        System.out.println("[WsAI] 开局 color=" + json.get("yourColor").getAsString()
                + " firstHand=" + json.get("firstHand").getAsBoolean()
                + " 红=" + json.get("redPlayerId").getAsString()
                + " 黑=" + json.get("blackPlayerId").getAsString());
        if (json.get("firstHand").getAsBoolean()) {
            thinkAndMove();
        }
    }

    private void handleMoveResult(JsonObject json) {
        if (!json.get("valid").getAsBoolean()) {
            return;
        }
        if (!json.has("move")) {
            return;
        }
        Move move = JsonMessages.parseMove(json.getAsJsonObject("move"));
        board.executeMove(move);
        if (json.has("flipResult")) {
            System.out.println("[WsAI] 翻出: " + json.get("flipResult").getAsString());
        }
        int nextTurn = board.getMoveCount() % 2 == 0 ? ChessPiece.RED : ChessPiece.BLACK;
        if (nextTurn == myColor) {
            thinkAndMove();
        }
    }

    private void thinkAndMove() {
        if (!inGame || !isOpen()) {
            return;
        }
        System.out.println("[WsAI] 思考中...");
        Move best = agent.selectMove(new Board(board), myColor, 55_000L);
        if (best == null) {
            System.out.println("[WsAI] 无合法着法，发送认输");
            sendJson(resignMessage());
            return;
        }
        System.out.println("[WsAI] 走: " + best);
        sendJson(moveMessage(best));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WsAI] 连接关闭: " + reason);
        running = false;
        loginLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WsAI] 错误: " + ex.getMessage());
    }

    private void sendJson(JsonObject obj) {
        send(JsonMessages.toJson(obj));
    }

    private JsonObject loginMessage() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.LOGIN);
        o.addProperty("userId", userId);
        o.addProperty("password", password);
        return o;
    }

    private JsonObject startMatchMessage() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.START_MATCH);
        return o;
    }

    private JsonObject readyMessage() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.READY);
        return o;
    }

    private JsonObject resignMessage() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.RESIGN);
        return o;
    }

    private JsonObject moveMessage(Move move) {
        JsonObject o = JsonMessages.moveToJson(move);
        o.addProperty("messageType", JsonMessageTypes.MOVE);
        ChessPiece piece = board.getPiece(move.getSource());
        boolean reveals = piece != null && !piece.isRevealed() && !move.isFlipOnly();
        o.addProperty("isFlip", move.isFlipOnly() || reveals);
        return o;
    }

    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://127.0.0.1:8887";
        String userId = args.length > 1 ? args[1] : "ai_bot_1";
        String password = args.length > 2 ? args[2] : "pw123";
        WsAIGameClient client = new WsAIGameClient(URI.create(url), userId, password);
        client.startAutoPlay();
    }
}

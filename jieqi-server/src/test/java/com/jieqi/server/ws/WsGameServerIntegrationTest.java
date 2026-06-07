package com.jieqi.server.ws;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import com.jieqi.protocol.json.BoardJsonMapper;
import com.jieqi.protocol.json.JsonErrorCodes;
import com.jieqi.protocol.json.JsonMessageTypes;
import com.jieqi.protocol.json.JsonMessages;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class WsGameServerIntegrationTest {

    private WsGameServer server;
    private int port;

    @BeforeEach
    void setUp() {
        port = 18080 + (int) (Math.random() * 1000);
        server = new WsGameServer(port);
        server.start();
        sleep(300);
    }

    @AfterEach
    void tearDown() {
        server.shutdown();
    }

    @Test
    void loginMatchReadyAndGameStart() throws Exception {
        TestWsClient p1 = connect("u1");
        TestWsClient p2 = connect("u2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "u1", "u2");
        assertNotNull(session.roomId());
        p1.close();
        p2.close();
    }

    @Test
    void pingReturnsPong() throws Exception {
        TestWsClient c = connect("pinger");
        c.sendJson(login("pinger", "x"));
        assertTrue(c.awaitType(JsonMessageTypes.LOGIN_RESULT, 3));
        var ping = JsonMessages.parse("{\"messageType\":\"ping\",\"timestamp\":123}");
        c.sendJson(JsonMessages.toJson(ping));
        JsonObject pong = c.awaitTypeObject(JsonMessageTypes.PONG, 3);
        assertEquals(123, pong.get("timestamp").getAsLong());
        c.close();
    }

    @Test
    void fullGameFlowWithMoveResultAndFlipResult() throws Exception {
        TestWsClient p1 = connect("red1");
        TestWsClient p2 = connect("blk1");
        GameStartSession session = loginMatchReadyStart(p1, p2, "red1", "blk1");
        TestWsClient red = session.red();
        TestWsClient black = session.black();
        JsonObject gsRed = red.findLastOfType(JsonMessageTypes.GAME_START);
        Board board = boardFromGameStart(gsRed);

        // 红方先手
        Move redMove = pickNonFlipMove(board, ChessPiece.RED);
        red.sendJson(moveJson(redMove, true));
        JsonObject r1 = red.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertTrue(r1.get("valid").getAsBoolean());
        assertTrue(r1.has("flipResult"));
        JsonObject r1b = black.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertTrue(r1b.get("valid").getAsBoolean());
        assertTrue(r1b.has("flipResult"));
        board.executeMove(redMove);

        // 黑方
        Move blackMove = pickNonFlipMove(board, ChessPiece.BLACK);
        black.sendJson(moveJson(blackMove, true));
        JsonObject b1 = black.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertTrue(b1.get("valid").getAsBoolean());
        assertTrue(b1.has("flipResult"));
        assertTrue(red.awaitType(JsonMessageTypes.MOVE_RESULT, 5));
        board.executeMove(blackMove);

        // 红方第三步
        Move redMove2 = pickNonFlipMove(board, ChessPiece.RED);
        red.sendJson(moveJson(redMove2, true));
        JsonObject r2 = red.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertTrue(r2.get("valid").getAsBoolean());
        assertTrue(r2.has("flipResult"));
        assertTrue(black.awaitType(JsonMessageTypes.MOVE_RESULT, 5));

        p1.close();
        p2.close();
    }

    @Test
    void illegalMoveReturnsError2001AndInvalidMoveResult() throws Exception {
        TestWsClient p1 = connect("ill1");
        TestWsClient p2 = connect("ill2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "ill1", "ill2");
        TestWsClient red = session.red();
        JsonObject gsRed = red.findLastOfType(JsonMessageTypes.GAME_START);
        Board board = boardFromGameStart(gsRed);

        red.clearMessages();
        red.sendJson(move("a", 4, "a", 5, false));
        JsonObject err = red.awaitError(JsonErrorCodes.ILLEGAL_MOVE, 5);
        assertEquals(JsonErrorCodes.ILLEGAL_MOVE, err.get("code").getAsInt());
        JsonObject mr = red.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertFalse(mr.get("valid").getAsBoolean());

        Move legal = pickNonFlipMove(board, ChessPiece.RED);
        red.sendJson(moveJson(legal, true));
        JsonObject ok = red.awaitValidMoveResult(5);
        assertNotNull(ok);
        assertTrue(ok.get("valid").getAsBoolean());

        p1.close();
        p2.close();
    }

    @Test
    void notYourTurnReturnsError2002() throws Exception {
        TestWsClient p1 = connect("turn1");
        TestWsClient p2 = connect("turn2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "turn1", "turn2");
        TestWsClient black = session.black();

        black.clearMessages();
        black.sendJson(move("b", 6, "b", 5, true));
        JsonObject err = black.awaitError(JsonErrorCodes.NOT_YOUR_TURN, 5);
        assertEquals(JsonErrorCodes.NOT_YOUR_TURN, err.get("code").getAsInt());
        JsonObject mr = black.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 5);
        assertFalse(mr.get("valid").getAsBoolean());

        p1.close();
        p2.close();
    }

    @Test
    void resignEndsGameForBothPlayers() throws Exception {
        TestWsClient p1 = connect("res1");
        TestWsClient p2 = connect("res2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "res1", "res2");
        String roomId = session.roomId();
        TestWsClient red = session.red();

        red.sendJson(resign());
        JsonObject overRed = red.awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        JsonObject overBlack = session.black().awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        assertEquals("resign", overRed.get("reason").getAsString());
        assertEquals("resign", overBlack.get("reason").getAsString());
        assertTrue(overRed.get("winner").getAsString().equals("black")
                || overRed.get("winner").getAsString().equals("red"));
        // 新行为：对局结束后房间保留一段时间等待 rematch 决定，不立即销毁。
        // 房间仍在 rooms map 中，但处于 finished 状态。
        assertTrue(server.hasRoomForTest(roomId));
        assertTrue(server.isFinishedRoomForTest(roomId));

        red.clearMessages();
        red.sendJson(move("a", 3, "a", 4, true));
        JsonObject mr = red.awaitTypeObject(JsonMessageTypes.MOVE_RESULT, 3);
        assertFalse(mr.get("valid").getAsBoolean());

        p1.close();
        p2.close();
    }

    @Test
    void agreedDrawEndsGameForBothPlayers() throws Exception {
        TestWsClient p1 = connect("draw1");
        TestWsClient p2 = connect("draw2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "draw1", "draw2");
        TestWsClient red = session.red();
        TestWsClient black = session.black();

        red.sendJson(drawOffer());
        JsonObject offerForRed = red.awaitTypeObject(JsonMessageTypes.DRAW_OFFERED, 5);
        JsonObject offerForBlack = black.awaitTypeObject(JsonMessageTypes.DRAW_OFFERED, 5);
        assertNotNull(offerForRed);
        assertNotNull(offerForBlack);
        assertEquals("draw1", offerForRed.get("fromUserId").getAsString());
        assertEquals("draw1", offerForBlack.get("fromUserId").getAsString());

        black.sendJson(drawAccept());
        JsonObject overRed = red.awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        JsonObject overBlack = black.awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        assertEquals("draw", overRed.get("winner").getAsString());
        assertEquals("draw", overBlack.get("winner").getAsString());
        assertEquals("draw_agreed", overRed.get("reason").getAsString());
        assertEquals("draw_agreed", overBlack.get("reason").getAsString());

        p1.close();
        p2.close();
    }

    @Test
    void declinedDrawNotifiesOffererAndGameContinues() throws Exception {
        TestWsClient p1 = connect("drawDecline1");
        TestWsClient p2 = connect("drawDecline2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "drawDecline1", "drawDecline2");
        TestWsClient red = session.red();
        TestWsClient black = session.black();
        JsonObject gsRed = red.findLastOfType(JsonMessageTypes.GAME_START);
        Board board = boardFromGameStart(gsRed);

        red.sendJson(drawOffer());
        assertNotNull(red.awaitTypeObject(JsonMessageTypes.DRAW_OFFERED, 5));
        assertNotNull(black.awaitTypeObject(JsonMessageTypes.DRAW_OFFERED, 5));
        black.sendJson(drawDecline());
        JsonObject declined = red.awaitTypeObject(JsonMessageTypes.DRAW_DECLINED, 5);
        assertNotNull(declined);
        assertEquals("drawDecline2", declined.get("fromUserId").getAsString());

        Move legal = pickNonFlipMove(board, ChessPiece.RED);
        red.sendJson(moveJson(legal, true));
        JsonObject ok = red.awaitValidMoveResult(5);
        assertNotNull(ok);
        assertTrue(ok.get("valid").getAsBoolean());

        p1.close();
        p2.close();
    }

    @Test
    void timeoutBroadcastsTimeoutAndGameOver() throws Exception {
        TestWsClient p1 = connect("to1");
        TestWsClient p2 = connect("to2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "to1", "to2");
        String roomId = session.roomId();

        server.expireTurnForTest(roomId);
        JsonObject timeoutRed = session.red().awaitTypeObject(JsonMessageTypes.TIMEOUT, 5);
        JsonObject timeoutBlack = session.black().awaitTypeObject(JsonMessageTypes.TIMEOUT, 5);
        assertEquals("timeout", timeoutRed.get("reason").getAsString());
        assertNotNull(timeoutRed.get("loserId").getAsString());
        assertNotNull(timeoutRed.get("winnerId").getAsString());

        JsonObject overRed = session.red().awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        JsonObject overBlack = session.black().awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        assertEquals("timeout", overRed.get("reason").getAsString());
        assertEquals("timeout", overBlack.get("reason").getAsString());

        p1.close();
        p2.close();
    }

    @Test
    void disconnectEndsGameForOpponent() throws Exception {
        TestWsClient p1 = connect("dc1");
        TestWsClient p2 = connect("dc2");
        GameStartSession session = loginMatchReadyStart(p1, p2, "dc1", "dc2");
        TestWsClient red = session.red();
        TestWsClient black = session.black();

        red.close();
        sleep(500);
        JsonObject over = black.awaitTypeObject(JsonMessageTypes.GAME_OVER, 5);
        assertEquals("disconnect", over.get("reason").getAsString());
        black.close();
    }

    @Test
    void requestFirstHandSwapsColorsWhenBlackWantsFirst() throws Exception {
        TestWsClient p1 = connect("fh1");
        TestWsClient p2 = connect("fh2");
        login(p1, "fh1");
        login(p2, "fh2");
        p1.sendJson(startMatch());
        p2.sendJson(startMatch());
        assertTrue(p1.awaitType(JsonMessageTypes.MATCH_SUCCESS, 5));
        assertTrue(p2.awaitType(JsonMessageTypes.MATCH_SUCCESS, 5));

        // 初始 p1=红 p2=黑；黑方请求先手
        p1.sendJson(requestFirstHand(false));
        p2.sendJson(requestFirstHand(true));
        p1.sendJson(ready());
        p2.sendJson(ready());

        JsonObject gs1 = p1.awaitTypeObject(JsonMessageTypes.GAME_START, 15);
        JsonObject gs2 = p2.awaitTypeObject(JsonMessageTypes.GAME_START, 15);

        // p2 应变为红方先手
        assertEquals("black", gs1.get("yourColor").getAsString());
        assertFalse(gs1.get("firstHand").getAsBoolean());
        assertEquals("red", gs2.get("yourColor").getAsString());
        assertTrue(gs2.get("firstHand").getAsBoolean());

        p1.close();
        p2.close();
    }

    @Test
    void unknownMessageTypeReturnsError4001() throws Exception {
        TestWsClient c = connect("unk");
        login(c, "unk");
        c.sendJson("{\"messageType\":\"foobar\"}");
        JsonObject err = c.awaitError(JsonErrorCodes.JSON_FORMAT, 3);
        assertEquals(JsonErrorCodes.JSON_FORMAT, err.get("code").getAsInt());
        c.close();
    }

    @Test
    void startMatchWithoutLoginReturnsError1001() throws Exception {
        TestWsClient c = connect("nologin");
        c.sendJson(startMatch());
        JsonObject err = c.awaitError(JsonErrorCodes.LOGIN_FAILED, 3);
        assertEquals(JsonErrorCodes.LOGIN_FAILED, err.get("code").getAsInt());
        c.close();
    }

    @Test
    void cancelMatchNotifiesOpponentAndClearsRoom() throws Exception {
        TestWsClient p1 = connect("can1");
        TestWsClient p2 = connect("can2");
        login(p1, "can1");
        login(p2, "can2");
        p1.sendJson(startMatch());
        p2.sendJson(startMatch());
        JsonObject ms1 = p1.awaitTypeObject(JsonMessageTypes.MATCH_SUCCESS, 5);
        assertTrue(p2.awaitType(JsonMessageTypes.MATCH_SUCCESS, 5));
        String roomId = ms1.get("roomId").getAsString();

        p1.sendJson(cancelMatch());
        JsonObject err = p2.awaitError(JsonErrorCodes.MATCH_FAILED, 5);
        assertEquals(JsonErrorCodes.MATCH_FAILED, err.get("code").getAsInt());
        assertFalse(server.hasRoomForTest(roomId));
        p1.close();
        p2.close();
    }

    // ── 辅助方法 ────────────────────────────────────────────

    private GameStartSession loginMatchReadyStart(TestWsClient p1, TestWsClient p2,
                                                   String u1, String u2) throws Exception {
        login(p1, u1);
        login(p2, u2);
        p1.sendJson(startMatch());
        p2.sendJson(startMatch());
        JsonObject ms1 = p1.awaitTypeObject(JsonMessageTypes.MATCH_SUCCESS, 5);
        assertTrue(p2.awaitType(JsonMessageTypes.MATCH_SUCCESS, 5));
        p1.sendJson(ready());
        p2.sendJson(ready());
        JsonObject gs1 = p1.awaitTypeObject(JsonMessageTypes.GAME_START, 15);
        JsonObject gs2 = p2.awaitTypeObject(JsonMessageTypes.GAME_START, 15);
        String roomId = ms1.get("roomId").getAsString();
        TestWsClient red;
        TestWsClient black;
        if ("red".equals(gs1.get("yourColor").getAsString())) {
            red = p1;
            black = p2;
        } else {
            red = p2;
            black = p1;
        }
        assertTrue(gs1.get("firstHand").getAsBoolean() || gs2.get("firstHand").getAsBoolean());
        return new GameStartSession(roomId, red, black);
    }

    private void login(TestWsClient c, String user) {
        c.sendJson(login(user, "pw"));
        assertTrue(c.awaitType(JsonMessageTypes.LOGIN_RESULT, 3));
    }

    private TestWsClient connect(String name) throws Exception {
        TestWsClient c = new TestWsClient(new URI("ws://127.0.0.1:" + port), name);
        c.connectBlocking(3, TimeUnit.SECONDS);
        return c;
    }

    private static String login(String user, String pass) {
        return "{\"messageType\":\"Login\",\"userId\":\"" + user + "\",\"password\":\"" + pass + "\"}";
    }

    private static String startMatch() {
        return "{\"messageType\":\"startMatch\"}";
    }

    private static String cancelMatch() {
        return "{\"messageType\":\"cancelMatch\"}";
    }

    private static String ready() {
        return "{\"messageType\":\"Ready\"}";
    }

    private static String resign() {
        return "{\"messageType\":\"Resign\"}";
    }

    private static String drawOffer() {
        return "{\"messageType\":\"drawOffer\"}";
    }

    private static String drawAccept() {
        return "{\"messageType\":\"drawAccept\"}";
    }

    private static String drawDecline() {
        return "{\"messageType\":\"drawDecline\"}";
    }

    private static String requestFirstHand(boolean wanna) {
        return "{\"messageType\":\"requestFirstHand\",\"wannaFirst\":" + wanna + "}";
    }

    private static String move(String fx, int fy, String tx, int ty, boolean isFlip) {
        return String.format(
                "{\"messageType\":\"move\",\"fromX\":\"%s\",\"fromY\":%d,\"toX\":\"%s\",\"toY\":%d,\"isFlip\":%s}",
                fx, fy, tx, ty, isFlip);
    }

    private static Board boardFromGameStart(JsonObject gameStart) {
        Board board = new Board();
        JsonArray cells = gameStart.getAsJsonArray("initialBoard");
        BoardJsonMapper.applyInitialBoard(board, cells);
        return board;
    }

    private static Move pickNonFlipMove(Board board, int color) {
        List<Move> moves = RuleValidator.generateAllMoves(board, color);
        for (Move m : moves) {
            if (!m.isFlipOnly()) {
                return m;
            }
        }
        throw new IllegalStateException("no legal non-flip move for color " + color);
    }

    private static String moveJson(Move m, boolean isFlip) {
        String src = m.getSource();
        String dst = m.getDestination();
        return move(
                String.valueOf(src.charAt(0)),
                Character.getNumericValue(src.charAt(1)),
                String.valueOf(dst.charAt(0)),
                Character.getNumericValue(dst.charAt(1)),
                isFlip);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    record GameStartSession(String roomId, TestWsClient red, TestWsClient black) {}

    static final class TestWsClient extends WebSocketClient {
        private final List<String> messages = new ArrayList<>();

        TestWsClient(URI uri, String name) {
            super(uri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {}

        @Override
        public void onMessage(String message) {
            synchronized (messages) {
                messages.add(message);
                messages.notifyAll();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {}

        @Override
        public void onError(Exception ex) {}

        void sendJson(String json) {
            super.send(json);
        }

        void clearMessages() {
            synchronized (messages) {
                messages.clear();
            }
        }

        boolean awaitType(String type, int seconds) {
            return awaitTypeObject(type, seconds) != null;
        }

        JsonObject awaitTypeObject(String type, int seconds) {
            long deadline = System.currentTimeMillis() + seconds * 1000L;
            synchronized (messages) {
                while (System.currentTimeMillis() < deadline) {
                    JsonObject last = findLastOfType(type);
                    if (last != null) {
                        return last;
                    }
                    long waitMs = Math.min(200, deadline - System.currentTimeMillis());
                    if (waitMs <= 0) {
                        break;
                    }
                    try {
                        messages.wait(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                return findLastOfType(type);
            }
        }

        JsonObject findLastOfType(String type) {
            JsonObject last = null;
            for (String m : messages) {
                if (m.contains("\"messageType\":\"" + type + "\"")) {
                    last = JsonMessages.parse(m);
                }
            }
            return last;
        }

        JsonObject awaitError(int code, int seconds) {
            long deadline = System.currentTimeMillis() + seconds * 1000L;
            synchronized (messages) {
                while (System.currentTimeMillis() < deadline) {
                    JsonObject last = findLastError(code);
                    if (last != null) {
                        return last;
                    }
                    long waitMs = Math.min(200, deadline - System.currentTimeMillis());
                    if (waitMs <= 0) {
                        break;
                    }
                    try {
                        messages.wait(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                return findLastError(code);
            }
        }

        JsonObject findLastError(int code) {
            JsonObject last = null;
            for (String m : messages) {
                if (m.contains("\"messageType\":\"error\"") && m.contains("\"code\":" + code)) {
                    last = JsonMessages.parse(m);
                }
            }
            return last;
        }

        JsonObject awaitValidMoveResult(int seconds) {
            long deadline = System.currentTimeMillis() + seconds * 1000L;
            synchronized (messages) {
                while (System.currentTimeMillis() < deadline) {
                    JsonObject last = findLastMoveResult();
                    if (last != null && last.get("valid").getAsBoolean()) {
                        return last;
                    }
                    long waitMs = Math.min(200, deadline - System.currentTimeMillis());
                    if (waitMs <= 0) {
                        break;
                    }
                    try {
                        messages.wait(waitMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
                JsonObject last = findLastMoveResult();
                if (last != null && last.get("valid").getAsBoolean()) {
                    return last;
                }
            }
            return null;
        }

        JsonObject findLastMoveResult() {
            JsonObject last = null;
            for (String m : messages) {
                if (m.contains("\"messageType\":\"" + JsonMessageTypes.MOVE_RESULT + "\"")) {
                    last = JsonMessages.parse(m);
                }
            }
            return last;
        }
    }
}

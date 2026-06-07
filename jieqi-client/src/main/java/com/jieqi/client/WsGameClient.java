package com.jieqi.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.json.BoardJsonMapper;
import com.jieqi.protocol.json.PieceJsonMapper;
import com.jieqi.protocol.json.JsonMessageTypes;
import com.jieqi.protocol.json.JsonMessages;
import com.jieqi.ui.ConsoleUI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 老师公共接口 — WebSocket JSON 控制台客户端。
 */
public class WsGameClient extends WebSocketClient {

    private final ConsoleUI ui = new ConsoleUI();
    private final Board board = new Board();
    private final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    private String userId;
    private String password;
    private String opponentNickname;
    private int myColor = -1;
    private boolean firstHand;
    private boolean inGame;
    private volatile boolean running = true;
    private final CountDownLatch loginLatch = new CountDownLatch(1);

    public WsGameClient(URI uri, String userId, String password) {
        super(uri);
        this.userId = userId;
        this.password = password;
    }

    public void startInteractive() throws Exception {
        connectBlocking(5, TimeUnit.SECONDS);
        sendJson(loginMessage());
        loginLatch.await(5, TimeUnit.SECONDS);

        System.out.println("""
                命令:
                  match                          开始匹配
                  ready                          准备就绪
                  first <true|false>             请求先手（10s 窗口内）
                  move <fx> <fy> <tx> <ty> [flip]  走子（例: move a 6 a 5）
                  flip 示例: move a 6 a 6 flip     原地翻子
                  resign | ping | board | quit
                坐标: 列 a-i，行 0-9（0=红方底线，9=黑方底线）
                """);
        while (running && isOpen()) {
            System.out.print("\n> ");
            String line = console.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                close();
                break;
            }
            if (line.equalsIgnoreCase("match")) {
                sendJson(startMatch());
                System.out.println("[WS Client] 正在匹配，等待对手加入...");
                continue;
            }
            if (line.equalsIgnoreCase("ready")) {
                sendJson(ready());
                System.out.println("[WS Client] 你已准备，等待对手准备...");
                continue;
            }
            if (line.equalsIgnoreCase("board") || line.equalsIgnoreCase("b")) {
                if (myColor >= 0) {
                    ui.displayBoard(board, myColor);
                }
                continue;
            }
            if (line.equalsIgnoreCase("ping")) {
                JsonObject p = new JsonObject();
                p.addProperty("messageType", JsonMessageTypes.PING);
                p.addProperty("timestamp", System.currentTimeMillis());
                sendJson(p);
                continue;
            }
            if (line.equalsIgnoreCase("resign")) {
                JsonObject r = new JsonObject();
                r.addProperty("messageType", JsonMessageTypes.RESIGN);
                sendJson(r);
                continue;
            }
            if (line.startsWith("move ")) {
                handleMoveCommand(line.substring(5).trim());
                continue;
            }
            if (line.startsWith("first ")) {
                boolean wanna = line.contains("true");
                JsonObject fh = new JsonObject();
                fh.addProperty("messageType", JsonMessageTypes.REQUEST_FIRST_HAND);
                fh.addProperty("wannaFirst", wanna);
                sendJson(fh);
                continue;
            }
            System.out.println("未知命令");
        }
    }

    private void handleMoveCommand(String args) {
        if (!inGame) {
            System.out.println("对局未开始");
            return;
        }
        String[] p = args.split("\\s+");
        if (p.length < 4) {
            System.out.println("用法: move <fromX> <fromY> <toX> <toY> [flip]");
            return;
        }
        JsonObject m = new JsonObject();
        m.addProperty("messageType", JsonMessageTypes.MOVE);
        m.addProperty("fromX", p[0]);
        m.addProperty("fromY", Integer.parseInt(p[1]));
        m.addProperty("toX", p[2]);
        m.addProperty("toY", Integer.parseInt(p[3]));
        boolean flip = p.length >= 5 && ("flip".equalsIgnoreCase(p[4]) || "true".equalsIgnoreCase(p[4]));
        m.addProperty("isFlip", flip || (p[0] + p[1]).equals(p[2] + p[3]));
        sendJson(m);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WS Client] 已连接服务器");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonMessages.parse(message);
            String type = JsonMessages.messageType(json);
            switch (type) {
                case JsonMessageTypes.LOGIN_RESULT -> {
                    if (json.get("success").getAsBoolean()) {
                        System.out.println("[WS Client] 登录成功: " + json.get("userId").getAsString());
                    } else {
                        System.out.println("[WS Client] 登录失败: " + json.get("message").getAsString());
                    }
                    loginLatch.countDown();
                }
                case JsonMessageTypes.MATCH_SUCCESS -> {
                    opponentNickname = json.get("opponentNickname").getAsString();
                    System.out.println("[WS Client] 匹配成功 room="
                            + json.get("roomId").getAsString() + " 对手=" + opponentNickname
                            + " (id=" + json.get("opponentId").getAsString() + ")");
                }
                case JsonMessageTypes.ROOM_INFO -> System.out.println("[WS Client] 对手已准备: "
                        + json.get("opponentReady").getAsBoolean());
                case JsonMessageTypes.GAME_START -> handleGameStart(json);
                case JsonMessageTypes.MOVE_RESULT -> handleMoveResult(json);
                case JsonMessageTypes.TIMEOUT -> System.out.println("[WS Client] 超时: " + message);
                case JsonMessageTypes.GAME_OVER -> {
                    System.out.println("[WS Client] 对局结束: " + message);
                    inGame = false;
                }
                case JsonMessageTypes.PONG -> System.out.println("[WS Client] pong ts="
                        + json.get("timestamp").getAsLong());
                case JsonMessageTypes.ERROR -> System.out.println("[WS Client] 错误 "
                        + json.get("code").getAsInt() + ": " + json.get("message").getAsString());
                default -> System.out.println("[WS Client] << " + message);
            }
        } catch (Exception e) {
            System.out.println("[WS Client] 无法解析: " + message);
        }
    }

    private void handleGameStart(JsonObject json) {
        myColor = PieceJsonMapper.colorFromString(json.get("yourColor").getAsString());
        firstHand = json.get("firstHand").getAsBoolean();
        inGame = true;
        JsonArray cells = json.getAsJsonArray("initialBoard");
        BoardJsonMapper.applyInitialBoard(board, cells);
        System.out.println("[WS Client] 开局 yourColor=" + json.get("yourColor").getAsString()
                + " firstHand=" + firstHand
                + " 红方=" + json.get("redPlayerId").getAsString()
                + " 黑方=" + json.get("blackPlayerId").getAsString()
                + (opponentNickname != null ? " 对手昵称=" + opponentNickname : ""));
        ui.displayBoard(board, myColor);
    }

    private void handleMoveResult(JsonObject json) {
        if (!json.get("valid").getAsBoolean()) {
            System.out.println("[WS Client] 着法无效");
            return;
        }
        if (!json.has("move")) {
            return;
        }
        Move move = JsonMessages.parseMove(json.getAsJsonObject("move"));
        board.executeMove(move);
        if (json.has("flipResult")) {
            System.out.println("[WS Client] 翻出: " + json.get("flipResult").getAsString());
        }
        ui.displayBoard(board, myColor >= 0 ? myColor : ChessPiece.RED);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WS Client] 连接关闭: " + reason);
        running = false;
        loginLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WS Client] 错误: " + ex.getMessage());
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

    private JsonObject startMatch() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.START_MATCH);
        return o;
    }

    private JsonObject ready() {
        JsonObject o = new JsonObject();
        o.addProperty("messageType", JsonMessageTypes.READY);
        return o;
    }

    public static void main(String[] args) throws Exception {
        String url = args.length > 0 ? args[0] : "ws://127.0.0.1:8887";
        String userId = args.length > 1 ? args[1] : "player1";
        String password = args.length > 2 ? args[2] : "123456";
        WsGameClient client = new WsGameClient(URI.create(url), userId, password);
        client.startInteractive();
    }
}

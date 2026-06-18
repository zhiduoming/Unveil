package com.jieqi.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 老师公共接口 — WebSocket JSON 控制台客户端。
 */
public class WsGameClient extends WebSocketClient {

    private final ConsoleUI ui = new ConsoleUI();
    private Board board = new Board();
    private final Board replayBoard = new Board();
    private final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

    private String userId;
    private String password;
    private String opponentNickname;
    private String roomId;
    private String redPlayerId;
    private String blackPlayerId;
    private String selectedAiLevel;
    private int myColor = -1;
    private boolean firstHand;
    private boolean inGame;
    private boolean postGameMode;
    private boolean replayMenuMode;
    private int replayIndex = 0;
    private int replayTotal = 0;
    private int gameMoveCount;
    private final List<String> moveLines = new ArrayList<>();
    private String lastWinner;
    private String lastReason;
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
                  move <fx> <fy> <tx> <ty>  走子（例: move a 6 a 5）
                  resign | ping | board | quit
                  replay              查看最后一步复盘
                  replay <step>       查看指定步（0=开局）
                  replay-next | rn    复盘下一步
                  replay-prev | rp    复盘上一步
                  rematch             再来一局
                  watch <roomId>      观战（本组扩展）
                  stats               当前局面统计
                  moves | record      查看棋谱
                  ai easy|medium|hard 发起人机对弈
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
            if (replayMenuMode) {
                if (!handleReplayMenuCommand(line)) {
                    continue;
                }
                continue;
            }
            if (postGameMode) {
                if (handlePostGameCommand(line)) {
                    continue;
                }
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
            if (line.equalsIgnoreCase("stats")) {
                printStats();
                continue;
            }
            if (line.equalsIgnoreCase("moves") || line.equalsIgnoreCase("record")) {
                printMoveRecord();
                continue;
            }
            if (line.startsWith("watch ")) {
                String watchRoom = line.substring(6).trim();
                JsonObject w = new JsonObject();
                w.addProperty("messageType", JsonMessageTypes.WATCH);
                w.addProperty("roomId", watchRoom);
                sendJson(w);
                System.out.println("[WS Client] 观战请求已发送 room=" + watchRoom);
                continue;
            }
            if (line.equalsIgnoreCase("replay")) {
                if (postGameMode) {
                    enterReplayMenu();
                    continue;
                }
                if (roomId == null) {
                    System.out.println("尚未加入房间，无法复盘");
                    continue;
                }
                requestReplay(-1);
                continue;
            }
            if (line.startsWith("replay ")) {
                try {
                    int step = Integer.parseInt(line.substring(7).trim());
                    requestReplay(step);
                } catch (NumberFormatException e) {
                    System.out.println("用法: replay <stepIndex>");
                }
                continue;
            }
            if (line.equalsIgnoreCase("replay-next") || line.equalsIgnoreCase("rn")) {
                stepReplay(1);
                continue;
            }
            if (line.equalsIgnoreCase("replay-prev") || line.equalsIgnoreCase("rp")) {
                stepReplay(-1);
                continue;
            }
            if (line.equalsIgnoreCase("rematch")) {
                JsonObject rm = new JsonObject();
                rm.addProperty("messageType", JsonMessageTypes.REMATCH_REQUEST);
                sendJson(rm);
                System.out.println("[WS Client] 已发送再来一局邀请");
                continue;
            }
            if (line.startsWith("ai ")) {
                String level = line.substring(3).trim().toLowerCase();
                if (!level.equals("easy") && !level.equals("medium") && !level.equals("hard")) {
                    System.out.println("用法: ai easy|medium|hard");
                    continue;
                }
                selectedAiLevel = level;
                JsonObject ai = new JsonObject();
                ai.addProperty("messageType", JsonMessageTypes.START_AI_GAME);
                ai.addProperty("aiLevel", level);
                sendJson(ai);
                printAiLevelBanner(level);
                System.out.println("[WS Client] 正在创建人机对局 (" + level + ")...");
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
            System.out.println("用法: move <fromX> <fromY> <toX> <toY>");
            return;
        }
        if ((p[0] + p[1]).equals(p[2] + p[3])) {
            System.out.println("揭棋规则：禁止原地翻子，请输入移动走法（起点≠终点）。");
            return;
        }
        JsonObject m = new JsonObject();
        m.addProperty("messageType", JsonMessageTypes.MOVE);
        m.addProperty("fromX", p[0]);
        m.addProperty("fromY", Integer.parseInt(p[1]));
        m.addProperty("toX", p[2]);
        m.addProperty("toY", Integer.parseInt(p[3]));
        m.addProperty("isFlip", false);
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
                    roomId = json.get("roomId").getAsString();
                    opponentNickname = json.get("opponentNickname").getAsString();
                    System.out.println("[WS Client] 匹配成功 room="
                            + roomId + " 对手=" + opponentNickname
                            + " (id=" + json.get("opponentId").getAsString() + ")");
                }
                case JsonMessageTypes.ROOM_INFO -> System.out.println("[WS Client] 对手已准备: "
                        + json.get("opponentReady").getAsBoolean());
                case JsonMessageTypes.GAME_START -> handleGameStart(json);
                case JsonMessageTypes.MOVE_RESULT -> handleMoveResult(json);
                case JsonMessageTypes.TIMEOUT -> System.out.println("[WS Client] 超时: " + message);
                case JsonMessageTypes.GAME_OVER -> handleGameOver(json);
                case JsonMessageTypes.PONG -> System.out.println("[WS Client] pong ts="
                        + json.get("timestamp").getAsLong());
                case JsonMessageTypes.ERROR -> System.out.println("[WS Client] 错误 "
                        + json.get("code").getAsInt() + ": " + json.get("message").getAsString());
                case JsonMessageTypes.REPLAY_FRAME -> handleReplayFrame(json);
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
        postGameMode = false;
        replayMenuMode = false;
        gameMoveCount = 0;
        moveLines.clear();
        if (json.has("roomId")) {
            roomId = json.get("roomId").getAsString();
        }
        redPlayerId = json.get("redPlayerId").getAsString();
        blackPlayerId = json.get("blackPlayerId").getAsString();
        JsonArray cells = json.getAsJsonArray("initialBoard");
        board = BoardJsonMapper.fromInitialBoard(cells);
        System.out.println("[WS Client] 开局 yourColor=" + json.get("yourColor").getAsString()
                + " firstHand=" + firstHand
                + " 红方=" + redPlayerId
                + " 黑方=" + blackPlayerId
                + (opponentNickname != null ? " 对手昵称=" + opponentNickname : ""));
        if (selectedAiLevel != null) {
            printAiLevelBanner(selectedAiLevel);
        }
        ui.displayBoard(board, myColor);
    }

    private void handleGameOver(JsonObject json) {
        inGame = false;
        postGameMode = true;
        replayMenuMode = false;
        lastWinner = json.has("winner") ? json.get("winner").getAsString() : "—";
        lastReason = json.has("reason") ? json.get("reason").getAsString() : "—";
        String winnerLabel = switch (lastWinner) {
            case "red" -> "红方";
            case "black" -> "黑方";
            case "draw" -> "和棋";
            default -> lastWinner;
        };
        String recordPath = roomId != null ? "records/" + roomId + ".jieqi" : null;
        String replayPath = roomId != null ? "records/" + roomId + ".replay.json" : null;
        String redName = redPlayerId != null ? redPlayerId : "—";
        String blackName = blackPlayerId != null ? blackPlayerId : "—";

        System.out.println("\n===== 对局结束 =====");
        System.out.println("房间号   " + (roomId != null ? roomId : "—"));
        System.out.println("红方     " + redName);
        System.out.println("黑方     " + blackName);
        System.out.println("胜者     " + winnerLabel);
        System.out.println("原因     " + lastReason);
        System.out.println("总步数   " + gameMoveCount);
        if (json.has("capturedReveal")) {
            printCapturedReveal(json.getAsJsonArray("capturedReveal"));
        }
        if (recordPath != null) {
            System.out.println("棋谱已保存：" + recordPath);
        }
        if (replayPath != null) {
            System.out.println("复盘已保存：" + replayPath);
        }
        System.out.println("\n可选操作：");
        System.out.println("replay   查看复盘");
        System.out.println("rematch  再来一局");
        System.out.println("quit     退出");
    }

    private void printCapturedReveal(JsonArray captured) {
        if (captured == null || captured.isEmpty()) {
            return;
        }
        List<String> redTaken = new ArrayList<>();
        List<String> blackTaken = new ArrayList<>();
        for (int i = 0; i < captured.size(); i++) {
            JsonObject c = captured.get(i).getAsJsonObject();
            String color = c.has("color") ? c.get("color").getAsString() : "";
            String piece = c.has("piece") ? c.get("piece").getAsString() : "?";
            boolean wasDark = c.has("wasDark") && c.get("wasDark").getAsBoolean();
            String label = piece + (wasDark ? "(暗)" : "");
            if ("red".equals(color)) {
                redTaken.add(label);
            } else if ("black".equals(color)) {
                blackTaken.add(label);
            }
        }
        System.out.println("红方被吃 " + (redTaken.isEmpty() ? "无" : String.join(" ", redTaken)));
        System.out.println("黑方被吃 " + (blackTaken.isEmpty() ? "无" : String.join(" ", blackTaken)));
    }

    private void enterReplayMenu() {
        replayMenuMode = true;
        System.out.println("\n===== 复盘模式 =====");
        System.out.println("命令: n(下一步)  p(上一步)  0(开局)  end(终局)");
        System.out.println("      g 12(跳到第12步)  q(退出复盘)");
        requestReplay(0);
    }

    private boolean handlePostGameCommand(String line) {
        if (line.equalsIgnoreCase("replay")) {
            enterReplayMenu();
            return true;
        }
        if (line.equalsIgnoreCase("rematch")) {
            JsonObject rm = new JsonObject();
            rm.addProperty("messageType", JsonMessageTypes.REMATCH_REQUEST);
            sendJson(rm);
            System.out.println("[WS Client] 已发送再来一局邀请");
            return true;
        }
        if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
            return false;
        }
        System.out.println("终局菜单：replay / rematch / quit");
        return true;
    }

    private boolean handleReplayMenuCommand(String line) {
        if (line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit")) {
            replayMenuMode = false;
            System.out.println("已退出复盘");
            return true;
        }
        if (line.equalsIgnoreCase("n")) {
            stepReplay(1);
            return true;
        }
        if (line.equalsIgnoreCase("p")) {
            stepReplay(-1);
            return true;
        }
        if (line.equalsIgnoreCase("0")) {
            requestReplay(0);
            return true;
        }
        if (line.equalsIgnoreCase("end")) {
            if (replayTotal > 0) {
                requestReplay(replayTotal - 1);
            }
            return true;
        }
        if (line.startsWith("g ")) {
            try {
                int step = Integer.parseInt(line.substring(2).trim());
                requestReplay(step);
            } catch (NumberFormatException e) {
                System.out.println("用法: g <步数>");
            }
            return true;
        }
        System.out.println("复盘命令: n / p / 0 / end / g <步> / q");
        return true;
    }

    private void stepReplay(int delta) {
        int next = replayIndex + delta;
        if (next < 0) {
            System.out.println("已经是开局");
            return;
        }
        if (replayTotal > 0 && next >= replayTotal) {
            System.out.println("已经是最后一步");
            return;
        }
        requestReplay(next);
    }

    private void printAiLevelBanner(String level) {
        String label = switch (level.toLowerCase()) {
            case "easy" -> "入门 Easy";
            case "hard" -> "挑战 Hard";
            default -> "标准 Medium";
        };
        String algo = switch (level.toLowerCase()) {
            case "easy" -> "启发式随机 + TopK 选子";
            case "hard" -> "Belief Sampling + Alpha-Beta";
            default -> "Alpha-Beta 搜索 + 置换表";
        };
        long budget = "easy".equalsIgnoreCase(level) ? 500L : 5000L;
        System.out.println("===== 人机对局 =====");
        System.out.println("你: " + (myColor == ChessPiece.RED ? "红方" : "黑方")
                + "  vs  AI (" + label + ")");
        System.out.println("算法: " + algo);
        System.out.println("时间预算: " + budget + "ms");
        System.out.println("====================");
    }

    private void printStats() {
        if (!inGame && !postGameMode) {
            System.out.println("当前无进行中对局");
            return;
        }
        System.out.println("当前步数: " + gameMoveCount);
        System.out.println("轮到: " + (board.getMoveCount() % 2 == 0 ? "红方" : "黑方"));
        System.out.println("红方剩余: " + summarizePieces(ChessPiece.RED));
        System.out.println("黑方剩余: " + summarizePieces(ChessPiece.BLACK));
        System.out.println("无吃子计数: " + board.getNoCaptureCount());
        int opp = myColor >= 0 ? (myColor == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED) : ChessPiece.RED;
        System.out.println("当前将军: " + (RuleValidator.isInCheck(board, opp) ? "是" : "否"));
    }

    private String summarizePieces(int color) {
        StringBuilder sb = new StringBuilder();
        for (ChessPiece p : board.getPieces(color)) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(p.getTypeName());
        }
        return sb.isEmpty() ? "—" : sb.toString();
    }

    private void printMoveRecord() {
        if (moveLines.isEmpty()) {
            System.out.println("(暂无棋谱记录)");
            return;
        }
        for (String line : moveLines) {
            System.out.println(line);
        }
    }

    private void requestReplay(int stepIndex) {
        JsonObject req = new JsonObject();
        req.addProperty("messageType", JsonMessageTypes.REPLAY_REQUEST);
        if (stepIndex >= 0) {
            req.addProperty("stepIndex", stepIndex);
        }
        sendJson(req);
    }

    private void handleReplayFrame(JsonObject json) {
        replayIndex = json.get("stepIndex").getAsInt();
        replayTotal = json.get("totalSteps").getAsInt();
        Board rebuilt = BoardJsonMapper.fromBoardJson(json.getAsJsonArray("board"));
        replayBoard.clearAllPieces();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = rebuilt.getPiece(r, c);
                if (p != null) {
                    replayBoard.placePiece(new ChessPiece(p), r, c);
                }
            }
        }
        System.out.println("步数: " + replayIndex + " / " + Math.max(0, replayTotal - 1)
                + "    状态: " + json.get("status").getAsString()
                + "    当前轮到: " + json.get("currentTurn").getAsString());
        if (json.has("move")) {
            JsonObject move = json.getAsJsonObject("move");
            System.out.print("上一手: " + move.get("from").getAsString()
                    + " → " + move.get("to").getAsString());
            if (json.has("captured")) {
                JsonObject cap = json.getAsJsonObject("captured");
                String piece = cap.has("piece") ? cap.get("piece").getAsString() : "?";
                String dark = cap.has("wasDark") && cap.get("wasDark").getAsBoolean() ? "(暗)" : "";
                System.out.print("  吃: " + piece + dark);
            }
            System.out.println();
        } else if (replayIndex == 0) {
            System.out.println("（开局局面）");
        }
        ui.displayBoard(replayBoard, myColor >= 0 ? myColor : ChessPiece.RED);
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
        gameMoveCount++;
        moveLines.add(gameMoveCount + ".  " + move.getSource() + " " + move.getDestination());
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

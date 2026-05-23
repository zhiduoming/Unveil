package com.jieqi.client;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;
import com.jieqi.ui.ConsoleUI;
import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private ProtocolReader reader;
    private BufferedReader consoleReader;
    private int color;
    private String playerName;
    private String gameId;
    private Board board;
    private ConsoleUI ui;
    private boolean running;

    public GameClient(String host, int port, String playerName) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            reader = new ProtocolReader(socket.getInputStream());
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            this.playerName = playerName;
            this.board = new Board();
            this.ui = new ConsoleUI();
            this.running = true;
        } catch (IOException e) {
            System.err.println("无法连接服务器: " + e.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        try {
            System.out.print("请选择颜色 (0=红方/先手, 1=黑方/后手): ");
            String colorChoice = consoleReader.readLine();
            color = Integer.parseInt(colorChoice);
            System.out.print("输入游戏ID (留空自动匹配): ");
            String inputGameId = consoleReader.readLine();
            out.println(Protocol.buildLoginMsg(color, playerName, inputGameId));

            Thread receiver = new Thread(this::receiveMessages);
            receiver.start();

            printHelp();
            while (running) {
                System.out.print("\n> ");
                String input = consoleReader.readLine();
                if (input == null) break;
                input = input.trim();
                if (input.isEmpty()) continue;

                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    out.println(Protocol.buildMessage(Protocol.MSG_QUIT, ""));
                    break;
                }
                if (input.equalsIgnoreCase("board") || input.equalsIgnoreCase("b")) {
                    ui.displayBoard(board, color);
                    continue;
                }
                if (input.equalsIgnoreCase("help") || input.equalsIgnoreCase("h")) {
                    printHelp();
                    continue;
                }
                if (input.equalsIgnoreCase("resign") || input.equalsIgnoreCase("r")) {
                    out.println(Protocol.buildResignMsg());
                    System.out.println("已发送认输请求");
                    continue;
                }
                if (input.equalsIgnoreCase("draw") || input.equalsIgnoreCase("d")) {
                    out.println(Protocol.buildDrawOffer());
                    System.out.println("已发送提和请求");
                    continue;
                }
                if (input.startsWith("chat ") || input.startsWith("c ")) {
                    int spaceIdx = input.indexOf(' ');
                    String msg = input.substring(spaceIdx + 1).trim();
                    if (!msg.isEmpty()) {
                        out.println(Protocol.buildChatMsg(color, playerName, msg));
                    }
                    continue;
                }
                if (input.startsWith("flip ") || input.startsWith("f ")) {
                    int spaceIdx = input.indexOf(' ');
                    String coord = input.substring(spaceIdx + 1).trim();
                    Move move = new Move(coord, coord);
                    move.setFlipOnly(true);
                    if (!sendMoveIfLegal(move)) {
                        continue;
                    }
                    continue;
                }

                // 默认解析为走法：source destination
                String[] parts = input.split("\\s+");
                if (parts.length >= 2) {
                    Move move = new Move(parts[0], parts[1]);
                    if (!sendMoveIfLegal(move)) {
                        continue;
                    }
                } else {
                    System.out.println("格式错误。用法: <source> <destination>  或 flip <coord>  或 draw/resign/board/help/quit");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
    }

    private void printHelp() {
        System.out.println("\n命令列表:");
        System.out.println("  <src> <dst>    走子，如 b1 b3");
        System.out.println("  flip <coord>   原地翻子，如 flip a0");
        System.out.println("  draw / d       提和");
        System.out.println("  resign / r     认输");
        System.out.println("  board / b      显示棋盘");
        System.out.println("  chat <msg>     发送聊天");
        System.out.println("  help / h       显示帮助");
        System.out.println("  quit / exit    退出游戏");
    }

    private boolean sendMoveIfLegal(Move move) {
        if (!RuleValidator.isValidMove(board, move, color)) {
            System.out.println("[本地校验] 非法着法，未发送");
            return false;
        }
        if (!RuleValidator.isMoveLegal(board, move, color)) {
            System.out.println("[本地校验] 走子后将被将军，未发送");
            return false;
        }
        out.println(Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move)));
        return true;
    }

    private void receiveMessages() {
        try {
            FrameDecoder.DecodedFrame frame;
            while (running && (frame = reader.readFrame()) != null) {
                int msgType = frame.msgType();
                String data = frame.payload();
                switch (msgType) {
                    case Protocol.MSG_GAME_STATE:
                        handleGameState(data);
                        break;
                    case Protocol.MSG_MOVE:
                        handleMove(data);
                        break;
                    case Protocol.MSG_BOARD_STATE:
                        handleBoardState(data);
                        break;
                    case Protocol.MSG_ERROR:
                        handleError(data);
                        break;
                    case Protocol.MSG_GAME_OVER:
                        handleGameOver(data);
                        break;
                    case Protocol.MSG_DRAW_REQUEST:
                        handleDrawRequest(data);
                        break;
                    case Protocol.MSG_RESIGN:
                        handleResignNotify(data);
                        break;
                    case Protocol.MSG_CHAT:
                        handleChat(data);
                        break;
                    default:
                        // 未知消息类型，静默忽略
                        break;
                }
            }
        } catch (IOException e) {
            if (running) System.err.println("与服务器断开连接");
        }
    }

    private void handleGameState(String data) {
        String[] parts = data.split("\\|");
        String subType = parts[0];
        switch (subType) {
            case "LOGIN_ACK":
                gameId = parts[1];
                int assignedColor = Integer.parseInt(parts[2]);
                if (assignedColor != color) {
                    System.out.println("服务器将你的颜色从 " + Protocol.getColorName(color)
                            + " 调整为 " + Protocol.getColorName(assignedColor));
                    color = assignedColor;
                }
                System.out.println("已加入游戏: " + gameId
                        + "，颜色: " + Protocol.getColorName(color)
                        + "，状态: " + parts[3]);
                break;
            case "GAME_START":
                int firstMove = Integer.parseInt(parts[1]);
                System.out.println("\n=== 游戏开始！先手方: "
                        + Protocol.getColorName(firstMove) + " ===\n");
                ui.displayBoard(board, color);
                break;
            case "TURN_CHANGE":
                int turn = Integer.parseInt(parts[1]);
                if (turn == color) {
                    System.out.println("轮到你了");
                } else {
                    System.out.println("等待对方走子...");
                }
                break;
            case "PAUSE":
                System.out.println("[游戏暂停: " + (parts.length > 1 ? parts[1] : "未知原因") + "]");
                break;
            case "RESUME":
                System.out.println("[游戏恢复]");
                break;
        }
    }

    private void handleMove(String data) {
        Move move = Protocol.deserializeMove(data);
        if (move != null) {
            String actor = (board.getMoveCount() % 2 == 0)
                    ? Protocol.getColorName(ChessPiece.RED)
                    : Protocol.getColorName(ChessPiece.BLACK);
            System.out.println("\n" + actor + "走法: " + move);
            board.executeMove(move); // 在本地棋盘上执行
            ui.displayBoard(board, color);
        }
    }

    private void handleBoardState(String data) {
        int turn = Protocol.applyBoardState(board, data);
        if (turn < 0) {
            System.out.println("[警告] BOARD_STATE 解析失败");
            return;
        }
        ui.displayBoard(board, color);
    }

    private void handleError(String data) {
        String[] parts = data.split("\\|", 2);
        int errorCode = Integer.parseInt(parts[0]);
        String errorMsg = parts.length > 1 ? parts[1] : "未知错误";
        System.out.println("[错误 " + errorCode + "] " + errorMsg);
    }

    private void handleGameOver(String data) {
        String[] parts = data.split("\\|");
        int winner = Integer.parseInt(parts[0]);
        int reasonCode = Integer.parseInt(parts[1]);
        String reasonDesc = parts.length > 2 ? parts[2] : "";

        System.out.println("\n========================================");
        if (winner == -1) {
            System.out.println("  游戏结束：和棋");
        } else if (winner == color) {
            System.out.println("  游戏结束：你赢了！");
        } else {
            System.out.println("  游戏结束：你输了");
        }
        System.out.println("  原因: " + reasonDesc + " (code=" + reasonCode + ")");
        System.out.println("  胜方: " + (winner == -1 ? "无" : Protocol.getColorName(winner)));
        System.out.println("========================================\n");
        running = false;
    }

    private void handleDrawRequest(String data) {
        if (data.equals("OFFER")) {
            System.out.println("\n对方提和。输入 'draw' 同意，或继续走子拒绝。");
        } else if (data.equals("ACCEPT")) {
            System.out.println("对方同意了提和");
        } else if (data.equals("DECLINE")) {
            System.out.println("对方拒绝了提和");
        }
    }

    private void handleResignNotify(String data) {
        int resignColor = Integer.parseInt(data);
        System.out.println(Protocol.getColorName(resignColor) + "已认输");
    }

    private void handleChat(String data) {
        String[] parts = data.split("\\|", 3);
        int msgColor = Integer.parseInt(parts[0]);
        String sender = parts.length > 1 ? parts[1] : "?";
        String msg = parts.length > 2 ? parts[2] : "";
        System.out.println("[" + sender + "(" + Protocol.getColorName(msgColor) + ")]: " + msg);
    }

    private void cleanup() {
        running = false;
        try {
            if (out != null) out.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;
        String name = args.length > 2 ? args[2] : "Player";
        new GameClient(host, port, name).start();
    }
}

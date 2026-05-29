package com.jieqi.app;

import com.jieqi.ai.AIVsAIEnhanced;
import com.jieqi.ai.EnhancedAIEngine;
import com.jieqi.ai.PerformanceTest;
import com.jieqi.client.GameClient;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import com.jieqi.server.GameServer;
import com.jieqi.ui.ConsoleUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            runCli(args);
            return;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================");
        System.out.println("   揭棋对弈系统 (Unveil)");
        System.out.println("=================================");
        System.out.println("--- WebSocket + JSON（推荐，老师协议 8887）---");
        System.out.println("3. 启动 WebSocket 服务器");
        System.out.println("4. 启动 WebSocket 客户端");
        System.out.println("9. AI 经 WebSocket 自动对弈");
        System.out.println("--- TCP 附录 B（可选调试 8888）---");
        System.out.println("1. 启动 TCP 服务器");
        System.out.println("2. 启动 TCP 客户端");
        System.out.println("--- 本地 / AI ---");
        System.out.println("5. AI vs AI 自动对弈");
        System.out.println("6. 人 vs AI 对弈（本地）");
        System.out.println("7. AI 性能测试");
        System.out.println("8. 本地测试模式");
        System.out.print("请选择 (1-9): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1 -> startServer(scanner);
            case 2 -> startClient(scanner);
            case 3 -> startWsServer(scanner);
            case 4 -> startWsClient(scanner);
            case 5 -> AIVsAIEnhanced.runMatch(4, 5000);
            case 6 -> playVsAI(scanner);
            case 7 -> PerformanceTest.main(new String[]{});
            case 8 -> runLocalTest(scanner);
            case 9 -> startWsAI(scanner);
            default -> System.out.println("无效选择");
        }
        scanner.close();
    }

    private static void runCli(String[] args) {
        switch (args[0]) {
            case "server" -> {
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;
                new GameServer(port).start();
            }
            case "client" -> {
                String host = args.length > 1 ? args[1] : "127.0.0.1";
                int port = args.length > 2 ? Integer.parseInt(args[2]) : 8888;
                String name = args.length > 3 ? args[3] : "Player";
                new GameClient(host, port, name).start();
            }
            case "server-ws" -> {
                int port = args.length > 1 ? Integer.parseInt(args[1]) : 8887;
                new com.jieqi.server.ws.WsGameServer(port).start();
            }
            case "client-ws" -> {
                try {
                    String url = args.length > 1 ? args[1] : "ws://127.0.0.1:8887";
                    String user = args.length > 2 ? args[2] : "player1";
                    String pass = args.length > 3 ? args[3] : "123456";
                    new com.jieqi.client.WsGameClient(java.net.URI.create(url), user, pass).startInteractive();
                } catch (Exception e) {
                    System.err.println("client-ws 失败: " + e.getMessage());
                }
            }
            case "ai-ws" -> {
                try {
                    String url = args.length > 1 ? args[1] : "ws://127.0.0.1:8887";
                    String user = args.length > 2 ? args[2] : "ai_bot_1";
                    String pass = args.length > 3 ? args[3] : "pw123";
                    new com.jieqi.ai.WsAIGameClient(java.net.URI.create(url), user, pass).startAutoPlay();
                } catch (Exception e) {
                    System.err.println("ai-ws 失败: " + e.getMessage());
                }
            }
            default -> System.out.println("用法: Main [server|client|server-ws|client-ws|ai-ws] ...");
        }
    }

    private static void startServer(Scanner scanner) {
        System.out.print("端口 (默认8888): ");
        String portStr = scanner.nextLine();
        int port = portStr.isEmpty() ? 8888 : Integer.parseInt(portStr);
        new GameServer(port).start();
    }

    private static void startClient(Scanner scanner) {
        System.out.print("服务器地址 (默认127.0.0.1): ");
        String host = scanner.nextLine();
        host = host.isEmpty() ? "127.0.0.1" : host;
        System.out.print("端口 (默认8888): ");
        String portStr = scanner.nextLine();
        int port = portStr.isEmpty() ? 8888 : Integer.parseInt(portStr);
        System.out.print("玩家名称: ");
        String name = scanner.nextLine();
        new GameClient(host, port, name).start();
    }

    private static void playVsAI(Scanner scanner) {
        Board board = new Board();
        ConsoleUI ui = new ConsoleUI();
        EnhancedAIEngine ai = new EnhancedAIEngine("AI", ChessPiece.BLACK);
        final int humanColor = ChessPiece.RED;
        Move lastMove = null;
        System.out.println("你执红方（先手），AI执黑方（后手）");
        System.out.println("棋盘固定红方视角；带 * 的格子是上一步走子的源/目标");
        while (true) {
            ui.displayBoard(board, humanColor, lastMove);
            System.out.print("你的走法 (源 目标 / flip coord / quit): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) {
                break;
            }
            if (input.isEmpty()) {
                System.out.println("请输入走法，例如: a3 a4  或  flip e0");
                continue;
            }
            if (input.equalsIgnoreCase("board")) {
                continue;
            }
            Move move;
            if (input.startsWith("flip ")) {
                String coord = input.substring(5).trim();
                move = new Move(coord, coord);
                move.setFlipOnly(true);
            } else {
                String[] parts = input.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("格式错误：需要「源 目标」，例如 a3 a4");
                    continue;
                }
                move = new Move(parts[0], parts[1]);
            }
            if (!RuleValidator.isValidMove(board, move, humanColor)) {
                System.out.println("非法走法！");
                continue;
            }
            ChessPiece captured = board.executeMove(move);
            lastMove = move;
            if (checkGameEnd(board, captured, "红方")) {
                ui.displayBoard(board, humanColor, lastMove);
                break;
            }

            System.out.println("\n>>> AI 思考中...");
            Move aiMove = ai.calculateMove(board);
            if (aiMove == null) {
                System.out.println("AI无步，你获胜");
                break;
            }
            if (!RuleValidator.isValidMove(board, aiMove, ChessPiece.BLACK)) {
                System.out.println("AI非法走法，你获胜");
                break;
            }
            ChessPiece aiCaptured = board.executeMove(aiMove);
            lastMove = aiMove;
            System.out.println(">>> AI 走: " + describeMove(board, aiMove));
            ui.displayBoard(board, humanColor, lastMove);
            if (checkGameEnd(board, aiCaptured, "黑方")) {
                break;
            }
        }
    }

    private static String describeMove(Board board, Move move) {
        int[] dst = com.jieqi.core.Coordinate.toRowCol(move.getDestination());
        ChessPiece piece = board.getPiece(dst[0], dst[1]);
        String reveal = piece != null && piece.isRevealed()
                ? "，翻开为 " + ChessPiece.getTypeName(piece.getType(), piece.getColor())
                : "";
        return move.getSource() + " → " + move.getDestination() + reveal;
    }

    private static boolean checkGameEnd(Board board, ChessPiece captured, String winnerName) {
        if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
            System.out.println(winnerName + "获胜！(吃将)");
            return true;
        }
        int opp = winnerName.equals("红方") ? ChessPiece.BLACK : ChessPiece.RED;
        if (RuleValidator.isCheckmate(board, opp)) {
            System.out.println(winnerName + "将死获胜");
            return true;
        }
        if (board.getNoCaptureCount() >= 80) {
            System.out.println("和棋(40回合无吃子)");
            return true;
        }
        return false;
    }

    private static void startWsServer(Scanner scanner) {
        System.out.print("WebSocket 端口 (默认8887): ");
        String portStr = scanner.nextLine();
        int port = portStr.isEmpty() ? 8887 : Integer.parseInt(portStr);
        new com.jieqi.server.ws.WsGameServer(port).start();
    }

    private static void startWsClient(Scanner scanner) {
        System.out.print("WebSocket URL (默认 ws://127.0.0.1:8887): ");
        String url = scanner.nextLine();
        url = url.isEmpty() ? "ws://127.0.0.1:8887" : url;
        System.out.print("userId: ");
        String userId = scanner.nextLine();
        System.out.print("password: ");
        String password = scanner.nextLine();
        try {
            new com.jieqi.client.WsGameClient(java.net.URI.create(url), userId, password).startInteractive();
        } catch (Exception e) {
            System.err.println("WebSocket 客户端错误: " + e.getMessage());
        }
    }

    private static void startWsAI(Scanner scanner) {
        System.out.print("WebSocket URL (默认 ws://127.0.0.1:8887): ");
        String url = scanner.nextLine();
        url = url.isEmpty() ? "ws://127.0.0.1:8887" : url;
        System.out.print("AI userId (默认 ai_bot_1): ");
        String userId = scanner.nextLine();
        userId = userId.isEmpty() ? "ai_bot_1" : userId;
        System.out.print("password (默认 pw123): ");
        String password = scanner.nextLine();
        password = password.isEmpty() ? "pw123" : password;
        try {
            new com.jieqi.ai.WsAIGameClient(java.net.URI.create(url), userId, password).startAutoPlay();
        } catch (Exception e) {
            System.err.println("WebSocket AI 客户端错误: " + e.getMessage());
        }
    }

    private static void runLocalTest(Scanner scanner) {
        Board board = new Board();
        ConsoleUI ui = new ConsoleUI();
        int current = ChessPiece.RED;
        while (true) {
            ui.displayBoard(board, current);
            System.out.println("轮到: " + (current == ChessPiece.RED ? "红方" : "黑方"));
            System.out.print("走法 (源 目标 / flip coord / quit): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("quit")) {
                break;
            }
            if (input.equalsIgnoreCase("board")) {
                continue;
            }
            Move move;
            if (input.startsWith("flip ")) {
                String coord = input.substring(5).trim();
                move = new Move(coord, coord);
                move.setFlipOnly(true);
            } else {
                String[] parts = input.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("格式错误");
                    continue;
                }
                move = new Move(parts[0], parts[1]);
            }
            if (!RuleValidator.isValidMove(board, move, current)) {
                System.out.println("非法");
                continue;
            }
            ChessPiece captured = board.executeMove(move);
            if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
                System.out.println((current == ChessPiece.RED ? "红方" : "黑方") + "获胜");
                break;
            }
            int opp = (current == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
            if (RuleValidator.isCheckmate(board, opp)) {
                System.out.println((current == ChessPiece.RED ? "红方" : "黑方") + "将死获胜");
                break;
            }
            current = opp;
        }
    }
}

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
        System.out.println("1. 启动服务器");
        System.out.println("2. 启动客户端（人人对弈）");
        System.out.println("3. AI vs AI 自动对弈");
        System.out.println("4. 人 vs AI 对弈（本地）");
        System.out.println("5. AI性能测试");
        System.out.println("6. 本地测试模式");
        System.out.print("请选择 (1-6): ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1 -> startServer(scanner);
            case 2 -> startClient(scanner);
            case 3 -> AIVsAIEnhanced.runMatch(4, 5000);
            case 4 -> playVsAI(scanner);
            case 5 -> PerformanceTest.main(new String[]{});
            case 6 -> runLocalTest(scanner);
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
            default -> System.out.println("用法: Main [server|client] ...");
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
        int currentColor = ChessPiece.RED;
        System.out.println("你执红方（先手），AI执黑方（后手）");
        while (true) {
            ui.displayBoard(board, currentColor);
            if (currentColor == ChessPiece.RED) {
                System.out.print("你的走法 (源 目标 / flip coord / quit): ");
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
                if (!RuleValidator.isValidMove(board, move, ChessPiece.RED)) {
                    System.out.println("非法走法！");
                    continue;
                }
                ChessPiece captured = board.executeMove(move);
                if (checkGameEnd(board, captured, "红方")) {
                    break;
                }
                currentColor = ChessPiece.BLACK;
            } else {
                System.out.println("AI思考中...");
                Move aiMove = ai.calculateMove(board);
                if (aiMove == null) {
                    System.out.println("AI无步，你获胜");
                    break;
                }
                if (!RuleValidator.isValidMove(board, aiMove, ChessPiece.BLACK)) {
                    System.out.println("AI非法走法，你获胜");
                    break;
                }
                ChessPiece captured = board.executeMove(aiMove);
                System.out.println("AI走: " + aiMove);
                if (checkGameEnd(board, captured, "黑方")) {
                    break;
                }
                currentColor = ChessPiece.RED;
            }
        }
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

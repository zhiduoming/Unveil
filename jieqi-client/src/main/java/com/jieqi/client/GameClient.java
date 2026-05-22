package com.jieqi.client;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.Protocol;
import com.jieqi.ui.ConsoleUI;
import java.io.*;
import java.net.*;

public class GameClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
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
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
            System.out.println("请选择颜色 (0=红方/先手, 1=黑方/后手): ");
            String colorChoice = consoleReader.readLine();
            color = Integer.parseInt(colorChoice);
            out.println(Protocol.buildLoginMsg(color, playerName));

            Thread receiver = new Thread(this::receiveMessages);
            receiver.start();

            while (running) {
                System.out.print("\n走法(源 目标) 或 flip coord / board / quit: ");
                String input = consoleReader.readLine();
                if (input == null || input.equalsIgnoreCase("quit")) {
                    out.println(Protocol.buildMessage(Protocol.MSG_QUIT, ""));
                    break;
                }
                if (input.equalsIgnoreCase("board")) {
                    ui.displayBoard(board, color);
                    continue;
                }
                if (input.startsWith("flip ")) {
                    String coord = input.substring(5).trim();
                    Move move = new Move(coord, coord);
                    move.setFlipOnly(true);
                    out.println(Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move)));
                    continue;
                }
                String[] parts = input.split("\\s+");
                if (parts.length >= 2) {
                    Move move = new Move(parts[0], parts[1]);
                    out.println(Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(move)));
                } else {
                    System.out.println("格式错误");
                }
            }
        } catch (IOException e) { e.printStackTrace(); } finally { cleanup(); }
    }

    private void receiveMessages() {
        try {
            String line;
            while ((line = in.readLine()) != null && running) {
                int msgType = Integer.parseInt(line.split("\\|")[0]);
                String data = Protocol.parseData(line);
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
                        System.out.println("错误: " + data);
                        break;
                    case Protocol.MSG_GAME_OVER:
                        handleGameOver(data);
                        break;
                }
            }
        } catch (IOException e) { if (running) System.err.println("与服务器断开连接"); }
    }

    private void handleGameState(String data) {
        String[] parts = data.split("\\|");
        if (parts[0].equals("START")) {
            System.out.println("\n=== 游戏开始！你为" + (color == ChessPiece.RED ? "红方(先手)" : "黑方(后手)"));
        } else {
            gameId = parts[0];
            System.out.println("已加入游戏: " + gameId + "，等待对手...");
        }
    }

    private void handleMove(String data) {
        Move move = Protocol.deserializeMove(data);
        if (move != null) {
            System.out.println("\n对方走法: " + move);
            ui.displayBoard(board, color);
        }
    }

    private void handleBoardState(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length < 2) return;
        // 简化解析，实际应重建board对象，这里仅刷新显示
        ui.displayBoard(board, color);
    }

    private void handleGameOver(String data) {
        int winner = Integer.parseInt(data);
        System.out.println("\n游戏结束！" + (winner == ChessPiece.RED ? "红方" : winner == ChessPiece.BLACK ? "黑方" : "和棋"));
        running = false;
    }

    private void cleanup() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;
        String name = args.length > 2 ? args[2] : "Player";
        new GameClient(host, port, name).start();
    }
}
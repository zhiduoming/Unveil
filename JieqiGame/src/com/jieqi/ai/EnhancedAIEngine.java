package com.jieqi.ai;

import com.jieqi.core.*;
import com.jieqi.network.Protocol;
import java.io.*;
import java.net.*;

public class EnhancedAIEngine implements Runnable {
    private String name;
    private int color;
    private OptimizedAlphaBeta search;
    private Board board;
    private boolean running;

    public EnhancedAIEngine(String name, int color) {
        this.name = name;
        this.color = color;
        this.search = new OptimizedAlphaBeta();
        this.board = new Board();
        this.running = true;
    }

    @Override
    public void run() {
        System.out.println("[" + name + "] AI引擎启动，作为" + (color == ChessPiece.RED ? "红方" : "黑方"));
        try (Socket socket = new Socket("127.0.0.1", 8888);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(Protocol.buildLoginMsg(color, name));
            System.out.println("[" + name + "] 已连接到服务器");

            String line;
            while ((line = in.readLine()) != null && running) {
                int msgType = Integer.parseInt(line.split("\\|")[0]);
                String data = Protocol.parseData(line);
                switch (msgType) {
                    case Protocol.MSG_BOARD_STATE:
                        // 简化处理，不完整解析棋盘，演示用
                        // 实际应解析并更新board
                        Move aiMove = calculateMove();
                        if (aiMove != null) {
                            out.println(Protocol.buildMessage(Protocol.MSG_MOVE, Protocol.serializeMove(aiMove)));
                        }
                        break;
                    case Protocol.MSG_GAME_OVER:
                        running = false;
                        break;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Move calculateMove() {
        System.out.println("[" + name + "] 思考中...");
        OptimizedAlphaBeta.SearchResult result = search.search(board, color, 55000);
        if (result.bestMove != null) {
            System.out.println("[" + name + "] 走: " + result.bestMove + " (分数=" + result.score + ")");
            return result.bestMove;
        }
        return null;
    }

    public Move calculateMoveFast() {
        OptimizedAlphaBeta.SearchResult result = search.search(board, color, 5000);
        return result.bestMove;
    }

    public void reset() { board = new Board(); search.clearHeuristics(); }
    public OptimizedAlphaBeta getSearch() { return search; }
    
    // 新增：修复 getName() 报错的方法
    public String getName() {
        return name;
    }
}
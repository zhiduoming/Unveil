package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.protocol.FrameDecoder;
import com.jieqi.protocol.Protocol;
import com.jieqi.protocol.ProtocolReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class EnhancedAIEngine implements Runnable {
    private final String name;
    private final int color;
    private final OptimizedAlphaBeta search;
    private final JieqiAgent agent = new JieqiAgent();
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
             ProtocolReader reader = new ProtocolReader(socket.getInputStream())) {

            out.println(Protocol.buildLoginMsg(color, name));
            System.out.println("[" + name + "] 已连接到服务器");

            FrameDecoder.DecodedFrame frame;
            while (running && (frame = reader.readFrame()) != null) {
                switch (frame.msgType()) {
                    case Protocol.MSG_BOARD_STATE:
                        Protocol.applyBoardState(board, frame.payload());
                        if (Protocol.parseCurrentTurnFromBoardState(frame.payload()) == color) {
                            Move aiMove = calculateMove();
                            if (aiMove != null) {
                                out.println(Protocol.buildMessage(Protocol.MSG_MOVE,
                                        Protocol.serializeMove(aiMove)));
                            }
                        }
                        break;
                    case Protocol.MSG_MOVE:
                        Move move = Protocol.deserializeMove(frame.payload());
                        if (move != null) {
                            board.executeMove(move);
                        }
                        break;
                    case Protocol.MSG_GAME_OVER:
                        running = false;
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Move calculateMove() {
        return calculateMove(board);
    }

    /** 本地人机对弈：在调用方提供的当前局面上搜索（不修改传入棋盘）。 */
    public Move calculateMove(Board gameBoard) {
        System.out.println("[" + name + "] 思考中...");
        Board snapshot = new Board(gameBoard);
        Move move = agent.selectMove(snapshot, color, 55_000L);
        if (move != null) {
            System.out.println("[" + name + "] 走: " + move);
        }
        return move;
    }

    public Move calculateMoveFast() {
        return agent.selectMove(board, color, 5_000L);
    }

    public Move calculateMoveFast(Board gameBoard) {
        return agent.selectMove(new Board(gameBoard), color, 5_000L);
    }

    public void reset() {
        board = new Board();
        search.clearHeuristics();
    }

    public OptimizedAlphaBeta getSearch() {
        return search;
    }

    public String getName() {
        return name;
    }
}

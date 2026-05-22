package com.jieqi.ai;

import com.jieqi.core.*;
import com.jieqi.ui.ConsoleUI;

public class AIVsAIEnhanced {
    public static void main(String[] args) { runMatch(4, 5000); }

    public static void runMatch(int searchDepth, long timePerMove) {
        Board board = new Board();
        ConsoleUI ui = new ConsoleUI();
        EnhancedAIEngine redAI = new EnhancedAIEngine("红方AI", ChessPiece.RED);
        EnhancedAIEngine blackAI = new EnhancedAIEngine("黑方AI", ChessPiece.BLACK);
        int currentColor = ChessPiece.RED;
        int moveCount = 0;
        int totalNodes = 0;

        System.out.println("============================================");
        System.out.println("   AI vs AI 增强对弈   每步时间: " + timePerMove + "ms");
        System.out.println("============================================");
        long gameStart = System.currentTimeMillis();

        while (true) {
            ui.displayBoard(board, currentColor);
            EnhancedAIEngine ai = (currentColor == ChessPiece.RED) ? redAI : blackAI;
            System.out.println("[" + ai.getName() + "] 思考中（第" + (moveCount + 1) + "步）...");
            long moveStart = System.currentTimeMillis();
            Move move = ai.getSearch().search(board, currentColor, timePerMove).bestMove;
            long moveTime = System.currentTimeMillis() - moveStart;
            if (move == null) { System.out.println("无合法走法"); break; }
            totalNodes += ai.getSearch().getNodesSearched();
            System.out.println("[" + ai.getName() + "] 走: " + move + " (深度=" + ai.getSearch().getMaxDepthReached() + ", 耗时=" + moveTime + "ms)");

            ChessPiece captured = board.executeMove(move);
            moveCount++;
            if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
                System.out.println("🎉 " + ai.getName() + " 获胜！(吃将)");
                break;
            }
            int opp = (currentColor == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
            if (RuleValidator.isCheckmate(board, opp)) { System.out.println("🎉 " + ai.getName() + " 将死获胜"); break; }
            if (RuleValidator.isStalemate(board, opp)) { System.out.println("🤝 困毙和棋"); break; }
            if (board.getNoCaptureCount() >= 80) { System.out.println("🤝 40回合无吃子和棋"); break; }
            if (moveCount > 300) { System.out.println("🤝 步数限制和棋"); break; }
            currentColor = opp;
        }
        long gameTime = System.currentTimeMillis() - gameStart;
        System.out.println("\n对局结束 总步数:" + moveCount + " 耗时:" + gameTime + "ms 总节点:" + totalNodes);
        System.out.println(board.getGameRecord());
    }
}
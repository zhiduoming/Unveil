package com.jieqi.ui;

import com.jieqi.core.*;

public class ConsoleUI {
    public void displayBoard(Board board, int playerColor) {
        displayBoard(board, playerColor, null);
    }

    /** @param lastMove 上一步走子，源/目标格以 * 高亮 */
    public void displayBoard(Board board, int playerColor, Move lastMove) {
        int[] lastSrc = lastMove == null ? null : Coordinate.toRowCol(lastMove.getSource());
        int[] lastDst = lastMove == null ? null : Coordinate.toRowCol(lastMove.getDestination());
        System.out.println("\n=================================");
        System.out.println("  揭棋对弈 - " + (playerColor == ChessPiece.RED ? "红方(↓)" : "黑方(↑)") + "视角");
        if (lastMove != null) {
            System.out.println("  上一步: " + lastMove);
        }
        System.out.println("=================================");
        System.out.print("   ");
        for (int c = 0; c < 9; c++) System.out.print(" " + (char)('a' + c) + " ");
        System.out.println();
        for (int r = 0; r < 10; r++) {
            System.out.print((9 - r) + " ");
            for (int c = 0; c < 9; c++) {
                boolean mark = (lastSrc != null && lastSrc[0] == r && lastSrc[1] == c)
                        || (lastDst != null && lastDst[0] == r && lastDst[1] == c);
                ChessPiece p = board.getPiece(r, c);
                if (p == null) {
                    System.out.print(mark ? " * " : " . ");
                } else {
                    String symbol;
                    if (!p.isRevealed()) symbol = "暗";
                    else symbol = ChessPiece.getTypeName(p.getType(), p.getColor());
                    String cell = mark ? "*" + symbol + "*" : " " + symbol + " ";
                    if (p.getColor() == ChessPiece.RED) System.out.print("\033[31m" + cell + "\033[0m");
                    else System.out.print(cell);
                }
            }
            System.out.println(" " + (9 - r));
        }
        System.out.print("   ");
        for (int c = 0; c < 9; c++) System.out.print(" " + (char)('a' + c) + " ");
        System.out.println("\n=================================\n");
    }

    public void displayMoveHistory(Board board) {
        System.out.println("\n=== 走法历史 ===");
        for (Move m : board.getMoveHistory())
            System.out.println(m);
    }
}
package com.jieqi.ui;

import com.jieqi.core.*;

public class ConsoleUI {
    public void displayBoard(Board board, int playerColor) {
        System.out.println("\n=================================");
        System.out.println("  揭棋对弈 - " + (playerColor == ChessPiece.RED ? "红方(↓)" : "黑方(↑)") + "视角");
        System.out.println("=================================");
        System.out.print("   ");
        for (int c = 0; c < 9; c++) System.out.print(" " + (char)('a' + c) + " ");
        System.out.println();
        for (int r = 0; r < 10; r++) {
            System.out.print((9 - r) + " ");
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) System.out.print(" . ");
                else {
                    String symbol;
                    if (!p.isRevealed()) symbol = "暗";
                    else symbol = ChessPiece.getTypeName(p.getType(), p.getColor());
                    // 简易颜色：红字用ANSI，黑字正常
                    if (p.getColor() == ChessPiece.RED) System.out.print("\033[31m" + symbol + "\033[0m ");
                    else System.out.print(symbol + " ");
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
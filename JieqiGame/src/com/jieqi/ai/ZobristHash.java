package com.jieqi.ai;

import com.jieqi.core.*;
import java.util.Random;

public class ZobristHash {
    private static long[][][][] table;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        Random rand = new Random(0xABCD1234);
        table = new long[10][9][2][8];
        for (int r = 0; r < 10; r++)
            for (int c = 0; c < 9; c++)
                for (int col = 0; col < 2; col++)
                    for (int state = 0; state < 8; state++)
                        table[r][c][col][state] = rand.nextLong();
        initialized = true;
    }

    public static long computeHash(Board board) {
        if (!initialized) init();
        long hash = 0;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p != null) {
                    int state = p.isRevealed() ? p.getType() + 1 : 0;
                    hash ^= table[r][c][p.getColor()][state];
                }
            }
        }
        return hash;
    }
}
package com.jieqi.ai;

import com.jieqi.core.*;

public class HistoryHeuristic {
    private int[][] historyScore;
    private static final int MAX = 10 * 9 * 10 * 9;

    public HistoryHeuristic() {
        historyScore = new int[2][MAX];
    }

    private int encodeMove(int fromRow, int fromCol, int toRow, int toCol) {
        return (fromRow * 9 + fromCol) * (10 * 9) + (toRow * 9 + toCol);
    }

    public void recordMove(Move move, int color, int depth) {
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        int idx = encodeMove(src[0], src[1], dst[0], dst[1]);
        historyScore[color][idx] += depth * depth;
    }

    public int getScore(Move move, int color) {
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        int idx = encodeMove(src[0], src[1], dst[0], dst[1]);
        return historyScore[color][idx];
    }

    public void age() {
        for (int i = 0; i < 2; i++)
            for (int j = 0; j < MAX; j++)
                historyScore[i][j] /= 2;
    }

    public void clear() {
        historyScore = new int[2][MAX];
    }
}
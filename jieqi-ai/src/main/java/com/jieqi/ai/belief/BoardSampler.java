package com.jieqi.ai.belief;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 从 AI 公开展示棋盘采样对手暗子真实身份（信念状态）。
 */
public final class BoardSampler {

    private static final int[] DARK_POOL = {
            ChessPiece.ROOK, ChessPiece.ROOK,
            ChessPiece.KNIGHT, ChessPiece.KNIGHT,
            ChessPiece.CANNON, ChessPiece.CANNON,
            ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN,
            ChessPiece.ADVISOR, ChessPiece.ADVISOR,
            ChessPiece.BISHOP, ChessPiece.BISHOP
    };

    private BoardSampler() {}

    public static Board fromPublicView(Board publicView, int viewerColor, Random rng) {
        Board sample = new Board(publicView);
        int oppColor = viewerColor == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED;
        List<ChessPiece> hidden = new ArrayList<>();
        for (ChessPiece p : sample.getPieces(oppColor)) {
            if (!p.isRevealed()) {
                hidden.add(p);
            }
        }
        if (hidden.isEmpty()) {
            return sample;
        }
        List<Integer> pool = buildRemainingPool(sample, oppColor);
        Collections.shuffle(pool, rng);
        for (int i = 0; i < hidden.size() && i < pool.size(); i++) {
            hidden.get(i).setType(pool.get(i));
        }
        return sample;
    }

    private static List<Integer> buildRemainingPool(Board board, int oppColor) {
        int[] counts = new int[7];
        for (int t : DARK_POOL) {
            counts[t]++;
        }
        for (ChessPiece p : board.getPieces(oppColor)) {
            if (p.isRevealed() && p.getType() != ChessPiece.KING) {
                counts[p.getType()]--;
            }
        }
        List<Integer> pool = new ArrayList<>();
        for (int t = 0; t < counts.length; t++) {
            for (int c = 0; c < counts[t]; c++) {
                pool.add(t);
            }
        }
        return pool;
    }
}

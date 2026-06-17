package com.jieqi.ai.eval;

import com.jieqi.core.ChessPiece;

/** 评估与走法排序共享的子力/权重常量（与 {@link ChessPiece#getBaseValue} 对齐）。 */
public final class EvaluationConstants {

    private EvaluationConstants() {}

    public static final int[] PIECE_VALUES = {
            10_000, 900, 400, 450, 50, 200, 200
    };

    public static final int MOBILITY_WEIGHT = 3;
    public static final int DARK_PIECE_BONUS = 5;
    public static final int CROSSED_PAWN_BONUS = 100;
    public static final int CROSSED_MINOR_BONUS = 30;
    public static final int KING_SAFETY_CHECK_PENALTY = 150;
    public static final int REPETITION_PENALTY = 100_000;

    public static int pieceValue(int type) {
        if (type < 0 || type >= PIECE_VALUES.length) {
            return 0;
        }
        return PIECE_VALUES[type];
    }
}

package com.jieqi.ai.bot;

import com.jieqi.ai.eval.EvaluationConstants;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;

import java.util.List;

/** 走法启发式排序（MVV-LVA + 中心偏好）。 */
public final class MoveOrderer {

    private MoveOrderer() {}

    public static void sortByHeuristic(Board board, List<Move> moves, int color) {
        moves.sort((a, b) -> Integer.compare(score(board, b, color), score(board, a, color)));
    }

    public static int score(Board board, Move move, int color) {
        int sc = 0;
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        ChessPiece target = board.getPiece(dst[0], dst[1]);
        ChessPiece piece = board.getPiece(src[0], src[1]);
        if (target != null && piece != null) {
            sc += target.getValue() * 10 - piece.getValue();
            if (target.isRevealed() && target.getType() == ChessPiece.KING) {
                sc += 100_000;
            }
        }
        if (move.isFlipOnly()) {
            sc += 5000;
        } else if (piece != null && !piece.isRevealed()) {
            sc += 800;
        }
        sc += (8 - (Math.abs(dst[0] - 4) + Math.abs(dst[1] - 4))) * EvaluationConstants.MOBILITY_WEIGHT;
        return sc;
    }
}

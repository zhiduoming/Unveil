package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptimizedAlphaBetaTacticalTest {

    @Test
    void searchPrefersResolvingMajorPieceThreat() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 3), 0, 3);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 5), 9, 5);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 4, 4), 4, 4);
        board.placePiece(new ChessPiece(ChessPiece.PAWN, ChessPiece.RED, true, 5, 4), 5, 4);

        OptimizedAlphaBeta ai = new OptimizedAlphaBeta();
        OptimizedAlphaBeta.SearchResult result = ai.search(board, ChessPiece.BLACK, 1000);

        assertNotNull(result.bestMove);
        ChessPiece captured = board.executeMove(result.bestMove);
        assertFalse(hasPawnThreatOnBlackMajorPiece(board),
                "AI should not leave its rook/cannon/knight directly capturable by a pawn");
        board.undoMove(result.bestMove, captured);
    }

    private static boolean hasPawnThreatOnBlackMajorPiece(Board board) {
        for (Move move : RuleValidator.generateAllMoves(board, ChessPiece.RED)) {
            ChessPiece attacker = board.getPiece(move.getSource());
            ChessPiece target = board.getPiece(move.getDestination());
            if (attacker == null || target == null) {
                continue;
            }
            if (attacker.isRevealed()
                    && attacker.getType() == ChessPiece.PAWN
                    && target.getColor() == ChessPiece.BLACK
                    && target.isRevealed()
                    && isMajorPiece(target.getType())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMajorPiece(int type) {
        return type == ChessPiece.ROOK || type == ChessPiece.CANNON || type == ChessPiece.KNIGHT;
    }
}

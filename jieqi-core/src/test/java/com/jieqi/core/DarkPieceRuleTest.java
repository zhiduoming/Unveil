package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DarkPieceRuleTest {

    @Test
    void darkAdvisorCannotLeavePalace() {
        Board board = new Board();
        ChessPiece advisor = findUnrevealed(board, ChessPiece.RED, ChessPiece.ADVISOR);
        assertNotNull(advisor);
        String src = ChessPiece.toCoord(advisor.getRow(), advisor.getCol());
        Move crossRiver = new Move(src, "c3");
        assertFalse(RuleValidator.isValidMove(board, crossRiver, ChessPiece.RED));
    }

    @Test
    void revealedAdvisorCanMoveOutsidePalace() {
        Board board = new Board();
        ChessPiece advisor = board.getPiece("d0");
        assertNotNull(advisor);
        advisor.setRevealed(true);
        advisor.setType(ChessPiece.ADVISOR);
        Move move = new Move("d0", "c1");
        assertTrue(RuleValidator.isValidMove(board, move, ChessPiece.RED));
    }

    @Test
    void revealedAdvisorCanCrossRiver() {
        Board board = new Board();
        board.clearAllPieces();
        ChessPiece advisor = new ChessPiece(ChessPiece.ADVISOR, ChessPiece.RED, true, 4, 4);
        board.placePiece(advisor, 4, 4);

        assertTrue(RuleValidator.isValidMove(board, new Move("e5", "d6"), ChessPiece.RED));
    }

    @Test
    void revealedBishopCanCrossRiverButStillNeedsClearEye() {
        Board board = new Board();
        board.clearAllPieces();
        ChessPiece bishop = new ChessPiece(ChessPiece.BISHOP, ChessPiece.RED, true, 5, 2);
        board.placePiece(bishop, 5, 2);

        assertTrue(RuleValidator.isValidMove(board, new Move("c4", "e6"), ChessPiece.RED));

        board.placePiece(new ChessPiece(ChessPiece.PAWN, ChessPiece.RED, true, 4, 3), 4, 3);
        assertFalse(RuleValidator.isValidMove(board, new Move("c4", "e6"), ChessPiece.RED));
    }

    @Test
    void flipOnlyMoveIsNotLegalMove() {
        Board board = new Board();
        Move flip = new Move("a3", "a3");
        flip.setFlipOnly(true);

        assertFalse(RuleValidator.isValidMove(board, flip, ChessPiece.RED));
        assertFalse(RuleValidator.generateAllMoves(board, ChessPiece.RED).stream().anyMatch(Move::isFlipOnly));
    }

    private static ChessPiece findUnrevealed(Board board, int color, int type) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p != null && p.getColor() == color && !p.isRevealed()
                        && p.getVirtualType() == type) {
                    return p;
                }
            }
        }
        return null;
    }
}

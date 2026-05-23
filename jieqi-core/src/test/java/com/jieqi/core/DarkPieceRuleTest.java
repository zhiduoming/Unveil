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

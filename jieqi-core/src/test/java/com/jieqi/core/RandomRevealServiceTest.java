package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomRevealServiceTest {

    private final RandomRevealService service = new RandomRevealService();

    @Test
    void stripsClientTypeBeforeServerProcessing() {
        Move move = new Move("b3", "b3", ChessPiece.ROOK);
        service.sanitizeClientMove(move);
        assertNull(move.getType());
    }

    @Test
    void stampsRevealedTypeAfterFlip() {
        Board board = new Board();
        ChessPiece dark = board.getPiece("a3");
        assertNotNull(dark);
        assertFalse(dark.isRevealed());
        int trueType = dark.getType();

        Move move = new Move("a3", "a3");
        move.setType(ChessPiece.CANNON);
        service.sanitizeClientMove(move);
        board.executeMove(move);
        service.stampServerRevealType(move, board);

        assertEquals(trueType, move.getType());
        assertTrue(board.getPiece("a3").isRevealed());
    }
}

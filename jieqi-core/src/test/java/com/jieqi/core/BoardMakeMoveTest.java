package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardMakeMoveTest {

    @Test
    void makeMoveUnmakeMoveRestoresPosition() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        Move move = new Move("e5", "e6");
        String keyBefore = Board.positionKey(board, ChessPiece.RED);

        Board.MoveSnapshot snap = board.makeMove(move);
        assertNotNull(board.getPiece(3, 4));
        assertNull(board.getPiece(4, 4));

        board.unmakeMove(snap);
        assertEquals(keyBefore, Board.positionKey(board, ChessPiece.RED));
        assertNotNull(board.getPiece(4, 4));
        assertNull(board.getPiece(3, 4));
    }

    @Test
    void strictLegalMovesMatchCopyBasedLegality() {
        Board board = new Board();
        var strict = RuleValidator.generateStrictLegalMoves(board, ChessPiece.RED);
        assertFalse(strict.isEmpty());
        for (Move m : strict) {
            assertTrue(RuleValidator.isValidMove(board, m, ChessPiece.RED));
            assertTrue(RuleValidator.isMoveLegal(board, m, ChessPiece.RED));
        }
    }
}

package com.jieqi.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardUndoTest {

    @Test
    void undoRestoresMoveCountAndNoCaptureCount() {
        Board board = twoKings();
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        int mcBefore = board.getMoveCount();
        int ncBefore = board.getNoCaptureCount();

        Move move = new Move("e5", "e6");
        Board.MoveSnapshot snap = board.makeMove(move);
        assertEquals(mcBefore + 1, board.getMoveCount());
        assertEquals(ncBefore + 1, board.getNoCaptureCount());

        board.unmakeMove(snap);
        assertEquals(mcBefore, board.getMoveCount());
        assertEquals(ncBefore, board.getNoCaptureCount());
    }

    @Test
    void undoAfterCaptureRestoresPieceLists() {
        Board board = twoKings();
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);
        board.placePiece(new ChessPiece(ChessPiece.PAWN, ChessPiece.BLACK, true, 3, 4), 3, 4);

        int blackBefore = board.getPieces(ChessPiece.BLACK).size();
        Move capture = new Move("e5", "e6");
        Board.MoveSnapshot snap = board.makeMove(capture);
        assertEquals(blackBefore - 1, board.getPieces(ChessPiece.BLACK).size());

        board.unmakeMove(snap);
        assertEquals(blackBefore, board.getPieces(ChessPiece.BLACK).size());
        assertNotNull(board.getPiece(3, 4));
    }

    @Test
    void undoDarkPieceMoveRestoresRevealState() {
        Board board = twoKings();
        ChessPiece dark = new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, false, 6, 0);
        dark.setVirtualType(ChessPiece.PAWN);
        board.placePiece(dark, 6, 0);

        Move move = new Move("a3", "a4");
        Board.MoveSnapshot snap = board.makeMove(move);
        ChessPiece after = board.getPiece(5, 0);
        assertNotNull(after);
        assertTrue(after.isRevealed());

        board.unmakeMove(snap);
        ChessPiece restored = board.getPiece(6, 0);
        assertNotNull(restored);
        assertFalse(restored.isRevealed());
        assertEquals(ChessPiece.ROOK, restored.getType());
        assertEquals(ChessPiece.PAWN, restored.getVirtualType());
    }

    @Test
    void hundredMakeUnmakeCyclesPreservePositionKey() {
        Board board = new Board();
        String key = Board.positionKey(board, ChessPiece.RED);
        List<Move> legal = RuleValidator.generateStrictLegalMoves(board, ChessPiece.RED);
        assertFalse(legal.isEmpty());
        Move move = legal.get(0);

        for (int i = 0; i < 100; i++) {
            Board.MoveSnapshot snap = board.makeMove(move);
            board.unmakeMove(snap);
        }
        assertEquals(key, Board.positionKey(board, ChessPiece.RED));
    }

    @Test
    void searchStyleMakeUnmakeDoesNotDriftBoard() {
        Board board = new Board();
        String keyBefore = Board.positionKey(board, ChessPiece.RED);
        int mcBefore = board.getMoveCount();
        int ncBefore = board.getNoCaptureCount();

        for (Move move : RuleValidator.generateStrictLegalMoves(board, ChessPiece.RED)) {
            Board.MoveSnapshot snap = board.makeMove(move);
            board.unmakeMove(snap);
        }

        assertEquals(keyBefore, Board.positionKey(board, ChessPiece.RED));
        assertEquals(mcBefore, board.getMoveCount());
        assertEquals(ncBefore, board.getNoCaptureCount());
    }

    private static Board twoKings() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        return board;
    }
}

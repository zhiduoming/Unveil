package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardAiPublicViewTest {

    @Test
    void masksOpponentUnrevealedTrueType() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);

        ChessPiece hidden = new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, false, 0, 0);
        hidden.setVirtualType(ChessPiece.ROOK);
        board.placePiece(hidden, 0, 0);

        Board view = board.createAiPublicView(ChessPiece.RED);
        ChessPiece masked = view.getPiece(0, 0);

        assertNotNull(masked);
        assertFalse(masked.isRevealed());
        assertEquals(ChessPiece.UNKNOWN, masked.getType());
        assertEquals(ChessPiece.ROOK, masked.getVirtualType());
        assertTrue(view.isAiPublicView());
        assertEquals(ChessPiece.RED, view.getAiViewerColor());
    }

    @Test
    void keepsOwnUnrevealedTrueType() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);

        ChessPiece ownHidden = new ChessPiece(ChessPiece.CANNON, ChessPiece.RED, false, 9, 1);
        ownHidden.setVirtualType(ChessPiece.KNIGHT);
        board.placePiece(ownHidden, 9, 1);

        Board view = board.createAiPublicView(ChessPiece.RED);
        ChessPiece kept = view.getPiece(9, 1);

        assertEquals(ChessPiece.CANNON, kept.getType());
        assertEquals(ChessPiece.KNIGHT, kept.getVirtualType());
    }

    @Test
    void opponentSimulatedRevealUsesVirtualTypeOnPublicView() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);

        ChessPiece hidden = new ChessPiece(ChessPiece.CANNON, ChessPiece.BLACK, false, 0, 3);
        hidden.setVirtualType(ChessPiece.ADVISOR);
        board.placePiece(hidden, 0, 3);

        Board view = board.createAiPublicView(ChessPiece.RED);
        Move move = new Move("d9", "e8");
        assertTrue(RuleValidator.isValidMove(view, move, ChessPiece.BLACK));

        Board.MoveSnapshot snap = view.makeMove(move);
        ChessPiece moved = view.getPiece(1, 4);
        assertNotNull(moved);
        assertTrue(moved.isRevealed());
        assertEquals(ChessPiece.ADVISOR, moved.getType(), "搜索中对手翻开应只见虚拟身份");

        view.unmakeMove(snap);
        assertFalse(view.getPiece(0, 3).isRevealed());
        assertEquals(ChessPiece.UNKNOWN, view.getPiece(0, 3).getType());
    }
}

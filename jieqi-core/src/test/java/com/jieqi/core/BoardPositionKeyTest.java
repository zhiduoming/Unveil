package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Board.positionKey：长将/重复判定与 AI 规避共用的局面键。 */
class BoardPositionKeyTest {

    private static Board twoKings() {
        Board b = new Board();
        b.clearAllPieces();
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        b.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 3), 0, 3);
        return b;
    }

    @Test
    void sideToMoveChangesKey() {
        Board b = twoKings();
        assertNotEquals(Board.positionKey(b, ChessPiece.RED),
                Board.positionKey(b, ChessPiece.BLACK));
    }

    @Test
    void sameLayoutSameKey() {
        assertEquals(Board.positionKey(twoKings(), ChessPiece.RED),
                Board.positionKey(twoKings(), ChessPiece.RED));
    }

    @Test
    void darkPieceDoesNotLeakIdentity() {
        Board b = new Board();
        b.clearAllPieces();
        ChessPiece dark = new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, false, 5, 0);
        dark.setVirtualType(ChessPiece.CANNON);
        b.placePiece(dark, 5, 0);
        String key = Board.positionKey(b, ChessPiece.RED);
        assertTrue(key.contains("0?"), "红方暗子应以 \"0?\" 占位，不泄露真实身份");
        // 翻开后键应变化（出现真实类型编码）
        dark.setRevealed(true);
        assertNotEquals(key, Board.positionKey(b, ChessPiece.RED));
    }
}

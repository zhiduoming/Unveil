package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChessPieceCoordTest {

    @Test
    void a0IsRedBottomLeft() {
        int[] rc = ChessPiece.fromCoord("a0");
        assertEquals(9, rc[0]);
        assertEquals(0, rc[1]);
        assertEquals("a0", ChessPiece.toCoord(rc[0], rc[1]));
    }

    @Test
    void i9IsBlackTopRight() {
        int[] rc = ChessPiece.fromCoord("i9");
        assertEquals(0, rc[0]);
        assertEquals(8, rc[1]);
    }
}

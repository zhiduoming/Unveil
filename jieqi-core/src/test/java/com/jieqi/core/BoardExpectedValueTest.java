package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardExpectedValueTest {

    @Test
    void darkPoolAverageValueIsPositiveAtStart() {
        Board board = new Board();
        int redEv = board.getExpectedValue(ChessPiece.RED);
        int blackEv = board.getExpectedValue(ChessPiece.BLACK);
        assertTrue(redEv > 0);
        assertTrue(blackEv > 0);
        assertEquals(redEv, blackEv);
    }
}

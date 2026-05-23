package com.jieqi.core;

import com.jieqi.protocol.Protocol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardSyncTest {

    @Test
    void roundTripBoardStatePayload() {
        Board board = new Board();
        int turn = ChessPiece.RED;
        String payload = Protocol.parsePayload(Protocol.buildBoardState(board, turn));
        assertNotNull(payload);
        Board copy = new Board();
        int parsedTurn = Protocol.applyBoardState(copy, payload);
        assertEquals(turn, parsedTurn);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece a = board.getPiece(r, c);
                ChessPiece b = copy.getPiece(r, c);
                if (a == null) {
                    assertNull(b);
                } else {
                    assertNotNull(b);
                    assertEquals(a.getColor(), b.getColor());
                    assertEquals(a.isRevealed(), b.isRevealed());
                    if (a.isRevealed()) {
                        assertEquals(a.getType(), b.getType());
                    }
                }
            }
        }
    }
}

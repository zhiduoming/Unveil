package com.jieqi.record;

import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveNotationRevealedTest {

    @Test
    void formatAndParseRevealedType() {
        Move move = new Move("e2", "e5");
        move.setRevealedType(ChessPiece.ROOK);
        String notation = MoveNotation.format(move);
        assertTrue(notation.contains("[揭:" + ChessPiece.ROOK + "]"));

        Move parsed = MoveNotation.parse(notation);
        assertNotNull(parsed);
        assertEquals(ChessPiece.ROOK, parsed.getRevealedType());
        assertTrue(parsed.hasRevealed());
    }
}

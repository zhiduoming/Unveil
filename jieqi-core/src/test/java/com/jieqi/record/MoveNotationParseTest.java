package com.jieqi.record;

import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoveNotationParseTest {

    @Test
    void parseFlipMove() {
        Move move = MoveNotation.parse("a0-a0(1)[翻]");
        assertNotNull(move);
        assertEquals("a0", move.getSource());
        assertEquals("a0", move.getDestination());
        assertEquals(1, move.getType());
        assertTrue(move.isFlipOnly());
        assertEquals("a0-a0(1)[翻]", MoveNotation.format(move));
    }

    @Test
    void parseNormalMove() {
        Move move = MoveNotation.parse("b1-c3(2)");
        assertNotNull(move);
        assertEquals(2, move.getType());
        assertFalse(move.isFlipOnly());
        assertEquals("b1-c3(2)", MoveNotation.format(move));
    }
}

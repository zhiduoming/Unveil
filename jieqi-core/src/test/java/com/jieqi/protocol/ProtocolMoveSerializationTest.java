package com.jieqi.protocol;

import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProtocolMoveSerializationTest {

    @Test
    void roundTripMoveWithTypeAndFlipFlag() {
        Move original = new Move("b1", "c3");
        original.setType(2);
        original.setTurnStartTime(1_700_000_000_000L);
        original.setFlipOnly(false);

        String wire = Protocol.serializeMove(original);
        Move parsed = Protocol.deserializeMove(wire);

        assertNotNull(parsed);
        assertEquals(original.getSource(), parsed.getSource());
        assertEquals(original.getDestination(), parsed.getDestination());
        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getTurnStartTime(), parsed.getTurnStartTime());
        assertEquals(original.isFlipOnly(), parsed.isFlipOnly());
    }

    @Test
    void roundTripFlipOnlyMove() {
        Move flip = new Move("a0", "a0");
        flip.setFlipOnly(true);
        String wire = Protocol.serializeMove(flip);
        Move parsed = Protocol.deserializeMove(wire);
        assertNotNull(parsed);
        assertTrue(parsed.isFlipOnly());
        assertEquals("a0", parsed.getSource());
    }
}

package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    @Test
    void validatesFileAndRank() {
        assertTrue(Coordinate.isValid("a0"));
        assertTrue(Coordinate.isValid("i9"));
        assertFalse(Coordinate.isValid("j0"));
        assertFalse(Coordinate.isValid("a10"));
        assertFalse(Coordinate.isValid(null));
    }

    @Test
    void roundTripsRowCol() {
        int[] rc = Coordinate.toRowCol("a0");
        assertEquals(9, rc[0]);
        assertEquals(0, rc[1]);
        assertEquals("a0", Coordinate.format(rc[0], rc[1]));
    }
}

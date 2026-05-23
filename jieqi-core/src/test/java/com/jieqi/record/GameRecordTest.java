package com.jieqi.record;

import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordTest {

    @Test
    void exportsStandardNotationLines() {
        GameRecord record = new GameRecord();
        Move flip = new Move("a0", "a0");
        flip.setFlipOnly(true);
        flip.setType(1);
        record.append(flip);
        record.append(new Move("b1", "c3"));

        String text = record.exportText();
        assertTrue(text.contains("1. a0-a0(1)[翻]"));
        assertTrue(text.contains("2. b1-c3"));
    }

    @Test
    void moveNotationMatchesInterfaceSpec() {
        Move m = new Move("b1", "c3");
        m.setType(2);
        assertEquals("b1-c3(2)", MoveNotation.format(m));
    }
}

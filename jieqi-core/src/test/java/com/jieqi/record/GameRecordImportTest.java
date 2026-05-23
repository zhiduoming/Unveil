package com.jieqi.record;

import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordImportTest {

    @Test
    void parsesNumberedLinesAndSkipsComments() {
        GameRecord record = GameRecord.fromExportedLines(List.of(
                "# gameId=demo",
                "1. a0-a0(1)[翻]",
                "2. b1-c3",
                "",
                "(无着法记录)"
        ));
        assertEquals(2, record.getLines().size());
        assertEquals("a0-a0(1)[翻]", record.getLines().get(0));
        assertEquals("b1-c3", record.getLines().get(1));
    }

    @Test
    void roundTripExportAndImport() {
        GameRecord original = new GameRecord();
        Move flip = new Move("a0", "a0");
        flip.setFlipOnly(true);
        flip.setType(1);
        original.append(flip);
        original.append(new Move("b1", "c3"));

        GameRecord loaded = GameRecord.fromExportedLines(
                List.of(original.exportText().split("\n")));
        assertEquals(original.getLines(), loaded.getLines());
    }
}

package com.jieqi.server;

import com.jieqi.core.Game;
import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GameRecordStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void savesRecordFile() throws Exception {
        Game game = new Game("test01");
        game.getRecord().append(new Move("a3", "a4"));
        GameRecordStore store = new GameRecordStore(tempDir.toString());
        Path saved = store.save(game);
        assertTrue(Files.exists(saved));
        String text = Files.readString(saved);
        assertTrue(text.contains("test01"));
        assertTrue(text.contains("a3-a4"));
    }

    @Test
    void loadRoundTrip() throws Exception {
        Game game = new Game("roundtrip");
        game.getRecord().append(new Move("b1", "c3"));
        GameRecordStore store = new GameRecordStore(tempDir.toString());
        Path saved = store.save(game);
        assertEquals(1, store.load(saved).getLines().size());
        assertEquals("b1-c3", store.load(saved).getLines().get(0));
    }
}

package com.jieqi.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReplayRecordStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void save_writesReplayJsonWithAllFrames(@TempDir Path dir) throws Exception {
        Game game = new Game("room_test_replay");
        game.setStatus(Game.GameStatus.PLAYING);
        game.recordReplayInitialIfNeeded();

        Move move = findFirstLegalMove(game);
        assertNotNull(move);
        game.processMove(move, ChessPiece.RED);
        game.setStatus(Game.GameStatus.BLACK_WIN);
        game.setGameOverReason(com.jieqi.core.EndgameJudge.ProtocolReason.CHECKMATE);

        ReplayRecordStore store = new ReplayRecordStore(dir.toString());
        Path saved = store.save(game);
        assertNotNull(saved);
        assertTrue(Files.exists(saved));
        assertTrue(saved.getFileName().toString().endsWith(".replay.json"));

        String json = Files.readString(saved);
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("room_test_replay", root.get("gameId").getAsString());
        assertEquals("BLACK_WIN", root.get("status").getAsString());
        JsonArray frames = root.getAsJsonArray("frames");
        assertEquals(2, frames.size());
        assertEquals(0, frames.get(0).getAsJsonObject().get("stepIndex").getAsInt());
        assertFalse(frames.get(0).getAsJsonObject().has("move"));
        assertTrue(frames.get(0).getAsJsonObject().has("board"));
        JsonObject frame1 = frames.get(1).getAsJsonObject();
        assertEquals(1, frame1.get("stepIndex").getAsInt());
        assertTrue(frame1.has("move"));
        assertTrue(frame1.has("currentTurn"));
        assertTrue(frame1.has("status"));
    }

    @Test
    void save_returnsNullWhenTimelineEmpty() throws Exception {
        Game game = new Game("empty");
        ReplayRecordStore store = new ReplayRecordStore(tempDir.toString());
        assertNull(store.save(game));
    }

    private static Move findFirstLegalMove(Game game) {
        var board = game.getBoard();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null || p.getColor() != ChessPiece.RED) {
                    continue;
                }
                String from = com.jieqi.core.Coordinate.format(r, c);
                for (int tr = 0; tr < 10; tr++) {
                    for (int tc = 0; tc < 9; tc++) {
                        if (tr == r && tc == c) {
                            continue;
                        }
                        String to = com.jieqi.core.Coordinate.format(tr, tc);
                        Move m = new Move(from, to);
                        if (RuleValidator.isValidMove(board, m, ChessPiece.RED)
                                && RuleValidator.isMoveLegal(board, m, ChessPiece.RED)) {
                            return m;
                        }
                    }
                }
            }
        }
        return null;
    }
}

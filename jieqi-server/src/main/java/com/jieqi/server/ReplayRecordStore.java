package com.jieqi.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Game;
import com.jieqi.core.Move;
import com.jieqi.protocol.json.BoardJsonMapper;
import com.jieqi.protocol.json.JsonMessages;
import com.jieqi.protocol.json.PieceJsonMapper;
import com.jieqi.record.ReplayFrame;
import com.jieqi.record.ReplayTimeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** 对局结束后将复盘时间线写入 {@code records/<gameId>.replay.json}。 */
public class ReplayRecordStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path directory;

    public ReplayRecordStore(String baseDir) {
        this.directory = Paths.get(baseDir);
    }

    public Path save(Game game) throws IOException {
        ReplayTimeline timeline = game.getReplayTimeline();
        if (timeline.isEmpty()) {
            return null;
        }
        Files.createDirectories(directory);
        Path file = directory.resolve(game.getGameId() + ".replay.json");

        JsonObject root = new JsonObject();
        root.addProperty("gameId", game.getGameId());
        root.addProperty("status", game.getStatus().name());
        int reasonCode = game.getGameOverReason();
        if (reasonCode >= 0) {
            root.addProperty("reasonCode", JsonMessages.reasonFromProtocolCode(reasonCode));
        }

        JsonArray frames = new JsonArray();
        for (ReplayFrame frame : timeline.getFrames()) {
            frames.add(frameToJson(frame));
        }
        root.add("frames", frames);

        Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
        return file.toAbsolutePath();
    }

    static JsonObject frameToJson(ReplayFrame frame) {
        JsonObject o = new JsonObject();
        o.addProperty("stepIndex", frame.getStepIndex());
        o.addProperty("currentTurn", PieceJsonMapper.colorToString(frame.getCurrentTurn()));
        o.addProperty("status", frame.getStatus().name());
        o.addProperty("timestamp", frame.getTimestamp());
        Move move = frame.getMove();
        if (move != null) {
            JsonObject m = new JsonObject();
            m.addProperty("from", move.getSource());
            m.addProperty("to", move.getDestination());
            o.add("move", m);
        }
        ChessPiece captured = frame.getCaptured();
        if (captured != null) {
            o.add("captured", capturedJson(captured));
        }
        o.add("board", BoardJsonMapper.toReplayBoard(frame.getBoardSnapshot()));
        return o;
    }

    private static JsonObject capturedJson(ChessPiece captured) {
        JsonObject c = new JsonObject();
        c.addProperty("color", PieceJsonMapper.colorToString(captured.getColor()));
        c.addProperty("wasDark", !captured.isRevealed());
        c.addProperty("piece", PieceJsonMapper.toJsonName(captured.getType()));
        return c;
    }
}

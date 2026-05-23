package com.jieqi.server;

import com.jieqi.core.Game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 对局结束后将棋谱写入本地目录（默认 {@code records/}）。
 */
public class GameRecordStore {

    private final Path directory;

    public GameRecordStore(String baseDir) {
        this.directory = Paths.get(baseDir);
    }

    public Path save(Game game) throws IOException {
        Files.createDirectories(directory);
        Path file = directory.resolve(game.getGameId() + ".jieqi");
        String header = "# gameId=" + game.getGameId()
                + " red=" + nullToEmpty(game.getRedPlayerName())
                + " black=" + nullToEmpty(game.getBlackPlayerName())
                + " status=" + game.getStatus()
                + System.lineSeparator();
        String body = game.getRecord().exportText();
        if (body.isEmpty()) {
            body = "(无着法记录)\n";
        }
        Files.writeString(file, header + body, StandardCharsets.UTF_8);
        return file.toAbsolutePath();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}

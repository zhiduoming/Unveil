package com.jieqi.record;

import com.jieqi.core.Board;
import com.jieqi.core.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 棋谱记录：服务器/客户端可共用，格式与 {@link Board#getGameRecord()} 一致。
 */
public class GameRecord {

    private static final Pattern NUMBERED_LINE = Pattern.compile("^\\d+\\.\\s*(.+)$");

    private final List<String> lines = new ArrayList<>();

    public void append(Move move) {
        lines.add(MoveNotation.format(move));
    }

    public void appendLine(String line) {
        lines.add(line);
    }

    public void loadFromBoard(Board board) {
        lines.clear();
        for (Move move : board.getMoveHistory()) {
            append(move);
        }
    }

    public List<String> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public String exportText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            sb.append(i + 1).append(". ").append(lines.get(i)).append('\n');
        }
        return sb.toString();
    }

    /**
     * 从导出行（可含 {@code 1. } 序号前缀与 {@code #} 注释）解析棋谱。
     */
    public static GameRecord fromExportedLines(Iterable<String> rawLines) {
        GameRecord record = new GameRecord();
        for (String raw : rawLines) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.equals("(无着法记录)")) {
                continue;
            }
            String notation = stripMoveNumber(line);
            Move move = MoveNotation.parse(notation);
            if (move != null) {
                record.append(move);
            }
        }
        return record;
    }

    static String stripMoveNumber(String line) {
        var matcher = NUMBERED_LINE.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1).trim();
        }
        return line;
    }

    @Override
    public String toString() {
        return exportText();
    }
}

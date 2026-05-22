package com.jieqi.record;

import com.jieqi.core.Board;
import com.jieqi.core.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 棋谱记录：服务器/客户端可共用，格式与 {@link Board#getGameRecord()} 一致。
 */
public class GameRecord {
    private final List<String> lines = new ArrayList<>();

    public void append(Move move) {
        lines.add(move.toString());
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

    @Override
    public String toString() {
        return exportText();
    }
}

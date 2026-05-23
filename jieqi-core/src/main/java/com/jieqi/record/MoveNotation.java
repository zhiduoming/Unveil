package com.jieqi.record;

import com.jieqi.core.Coordinate;
import com.jieqi.core.Move;

/**
 * 棋谱记法：{@code source-destination[(type)] [翻]}，对齐 INTERFACE.md §6.3。
 */
public final class MoveNotation {

    private MoveNotation() {}

    public static String format(Move move) {
        if (move == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(move.getSource()).append('-').append(move.getDestination());
        if (move.getType() != null) {
            sb.append('(').append(move.getType()).append(')');
        }
        if (move.isFlipOnly()) {
            sb.append("[翻]");
        }
        return sb.toString();
    }

    /**
     * 解析棋谱单行记法（不含序号前缀）。
     */
    public static Move parse(String notation) {
        if (notation == null || notation.isBlank()) {
            return null;
        }
        String rest = notation.trim();
        boolean flipOnly = rest.endsWith("[翻]");
        if (flipOnly) {
            rest = rest.substring(0, rest.length() - 3);
        }
        Integer type = null;
        int open = rest.indexOf('(');
        if (open >= 0) {
            int close = rest.indexOf(')', open);
            if (close < 0) {
                return null;
            }
            type = Integer.parseInt(rest.substring(open + 1, close));
            rest = rest.substring(0, open) + rest.substring(close + 1);
        }
        int dash = rest.indexOf('-');
        if (dash <= 0 || dash >= rest.length() - 1) {
            return null;
        }
        String source = rest.substring(0, dash);
        String dest = rest.substring(dash + 1);
        if (!Coordinate.isValid(source) || !Coordinate.isValid(dest)) {
            return null;
        }
        Move move = new Move(source, dest);
        if (type != null) {
            move.setType(type);
        }
        if (flipOnly) {
            move.setFlipOnly(true);
        }
        return move;
    }
}

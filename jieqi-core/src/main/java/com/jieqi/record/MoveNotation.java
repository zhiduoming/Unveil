package com.jieqi.record;

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
}

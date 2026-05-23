package com.jieqi.core;

/**
 * 坐标记法 a–i 列、0–9 行（与 INTERFACE.md §6 一致）。
 */
public final class Coordinate {

    private Coordinate() {}

    public static boolean isValid(String coord) {
        if (coord == null || coord.length() != 2) {
            return false;
        }
        char file = coord.charAt(0);
        char rank = coord.charAt(1);
        return file >= 'a' && file <= 'i' && rank >= '0' && rank <= '9';
    }

    public static int[] toRowCol(String coord) {
        if (!isValid(coord)) {
            throw new IllegalArgumentException("invalid coordinate: " + coord);
        }
        return new int[]{9 - (coord.charAt(1) - '0'), coord.charAt(0) - 'a'};
    }

    public static String format(int row, int col) {
        if (row < 0 || row > 9 || col < 0 || col > 8) {
            throw new IllegalArgumentException("row/col out of range: " + row + "," + col);
        }
        return "" + (char) ('a' + col) + (9 - row);
    }
}

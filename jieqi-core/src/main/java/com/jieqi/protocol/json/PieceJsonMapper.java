package com.jieqi.protocol.json;

import com.jieqi.core.ChessPiece;

/** 内部类型编码 ↔ 老师文档 piece / flipResult 英文枚举。 */
public final class PieceJsonMapper {

    private static final String[] NAMES = {
            "king", "rook", "knight", "cannon", "pawn", "guard", "bishop"
    };

    private PieceJsonMapper() {}

    public static String toJsonName(int type) {
        if (type < 0 || type >= NAMES.length) {
            return "unknown";
        }
        return NAMES[type];
    }

    public static int fromJsonName(String name) {
        if (name == null) {
            return ChessPiece.UNKNOWN;
        }
        String n = name.trim().toLowerCase();
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(n)) {
                return i;
            }
        }
        return ChessPiece.UNKNOWN;
    }

    public static String colorToString(int color) {
        return color == ChessPiece.RED ? "red" : "black";
    }

    public static int colorFromString(String s) {
        if (s == null) {
            return ChessPiece.RED;
        }
        return "black".equalsIgnoreCase(s.trim()) ? ChessPiece.BLACK : ChessPiece.RED;
    }
}

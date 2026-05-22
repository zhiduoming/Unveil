package com.jieqi.core;

public class ChessPiece {
    public static final int KING = 0;
    public static final int ROOK = 1;
    public static final int KNIGHT = 2;
    public static final int CANNON = 3;
    public static final int PAWN = 4;
    public static final int ADVISOR = 5;
    public static final int BISHOP = 6;
    public static final int UNKNOWN = -1;

    public static final int RED = 0;
    public static final int BLACK = 1;

    private int type;           // 实际类型（UNKNOWN表示暗子未翻开）
    private int virtualType;    // 虚拟类型：暗子按此类型规则移动
    private int color;
    private boolean revealed;
    private int row;
    private int col;

    public ChessPiece(int type, int color, boolean revealed, int row, int col) {
        this.type = type;
        this.color = color;
        this.revealed = revealed;
        this.row = row;
        this.col = col;
        this.virtualType = UNKNOWN;
    }

    public ChessPiece(ChessPiece other) {
        this.type = other.type;
        this.color = other.color;
        this.revealed = other.revealed;
        this.row = other.row;
        this.col = other.col;
        this.virtualType = other.virtualType;
    }

    public int getMoveType() {
        if (!revealed) return virtualType;
        return type;
    }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public int getVirtualType() { return virtualType; }
    public void setVirtualType(int vt) { this.virtualType = vt; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public boolean isRevealed() { return revealed; }
    public void setRevealed(boolean r) { this.revealed = r; }
    public int getRow() { return row; }
    public void setRow(int r) { this.row = r; }
    public int getCol() { return col; }
    public void setCol(int c) { this.col = c; }

    public String getTypeName() {
        if (!revealed) return "暗";
        return getTypeName(type, color);
    }

    public static String getTypeName(int type, int color) {
        String[] redNames = {"帅", "车", "马", "炮", "兵", "仕", "相"};
        String[] blackNames = {"将", "车", "马", "炮", "卒", "士", "象"};
        if (type == UNKNOWN) return "暗";
        return (color == RED ? redNames : blackNames)[type];
    }

    public static int getBaseValue(int type) {
        switch (type) {
            case KING:   return 10000;
            case ROOK:   return 600;
            case KNIGHT: return 270;
            case CANNON: return 285;
            case PAWN:   return 30;
            case ADVISOR:return 120;
            case BISHOP: return 120;
            default:     return 0;
        }
    }

    public int getValue() {
        if (!revealed) return getBaseValue(virtualType);
        return getBaseValue(type);
    }

    public static String toCoord(int row, int col) {
        return "" + (char)('a' + col) + (9 - row);
    }

    public static int[] fromCoord(String coord) {
        return new int[]{9 - (coord.charAt(1) - '0'), coord.charAt(0) - 'a'};
    }

    @Override
    public String toString() {
        return (color == RED ? "红" : "黑") + getTypeName() + "(" + toCoord(row, col) + ")";
    }
}
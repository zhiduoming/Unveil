package com.jieqi.ai;

import com.jieqi.core.*;
import java.util.*;

public class EnhancedEvaluator {
    private static final int[] PIECE_VALUES = {10000, 600, 270, 285, 30, 120, 120};
    private static int[][] ROOK_POS = new int[10][9];
    private static int[][] KNIGHT_POS = new int[10][9];
    private static int[][] CANNON_POS = new int[10][9];
    private static int[][] PAWN_POS = new int[10][9];

    static {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ROOK_POS[r][c] = (3 - Math.abs(c - 4)) * 5 + (5 - Math.abs(r - 4)) * 3;
                int centerDist = Math.abs(r - 4) + Math.abs(c - 4);
                KNIGHT_POS[r][c] = Math.max(0, 8 - centerDist) * 8;
                CANNON_POS[r][c] = (3 - Math.abs(c - 4)) * 3 + (r >= 3 && r <= 6 ? 10 : 0);
                if (r >= 5) PAWN_POS[r][c] = (r == 6 ? 0 : (9 - r) * 5);
                else PAWN_POS[r][c] = 30 + (5 - r) * 5;
                PAWN_POS[r][c] += (3 - Math.abs(c - 4)) * 2;
            }
        }
    }

    public static int evaluate(Board board, int currentColor) {
        int red = evaluateColor(board, ChessPiece.RED);
        int black = evaluateColor(board, ChessPiece.BLACK);
        int score = red - black;
        return currentColor == ChessPiece.RED ? score : -score;
    }

    private static int evaluateColor(Board board, int color) {
        int score = 0;
        // 子力
        score += evaluateMaterial(board, color);
        // 位置
        score += evaluatePosition(board, color);
        // 机动性
        score += RuleValidator.generateAllMoves(board, color).size() * 3;
        // 将帅安全
        score += evaluateKingSafety(board, color);
        // 威胁
        score += evaluateThreats(board, color);
        // 兵形
        score += evaluatePawnStructure(board, color);
        return score;
    }

    private static int evaluateMaterial(Board board, int color) {
        int material = 0;
        double darkValueSum = 0;
        int darkCount = 0;
        for (ChessPiece p : board.getPieces(color)) {
            if (p.isRevealed()) {
                material += PIECE_VALUES[p.getType()];
                boolean crossed = (color == ChessPiece.RED) ? p.getRow() <= 4 : p.getRow() >= 5;
                if (crossed) {
                    if (p.getType() == ChessPiece.PAWN) material += 20;
                    if (p.getType() == ChessPiece.ADVISOR || p.getType() == ChessPiece.BISHOP) material += 30;
                }
            } else {
                darkCount++;
                darkValueSum += ChessPiece.getBaseValue(p.getVirtualType());
            }
        }
        if (darkCount > 0) material += (int)(darkValueSum / darkCount) * darkCount;
        material += darkCount * 5;
        return material;
    }

    private static int evaluatePosition(Board board, int color) {
        int score = 0;
        for (ChessPiece p : board.getPieces(color)) {
            if (!p.isRevealed()) continue;
            int r = p.getRow(), c = p.getCol();
            if (color == ChessPiece.RED) {
                switch (p.getType()) {
                    case ChessPiece.ROOK:   score += ROOK_POS[r][c]; break;
                    case ChessPiece.KNIGHT: score += KNIGHT_POS[r][c]; break;
                    case ChessPiece.CANNON: score += CANNON_POS[r][c]; break;
                    case ChessPiece.PAWN:   score += PAWN_POS[r][c]; break;
                }
            } else {
                int flipR = 9 - r;
                switch (p.getType()) {
                    case ChessPiece.ROOK:   score += ROOK_POS[flipR][c]; break;
                    case ChessPiece.KNIGHT: score += KNIGHT_POS[flipR][c]; break;
                    case ChessPiece.CANNON: score += CANNON_POS[flipR][c]; break;
                    case ChessPiece.PAWN:   score += PAWN_POS[flipR][c]; break;
                }
            }
        }
        return score;
    }

    private static int evaluateKingSafety(Board board, int color) {
        ChessPiece king = null;
        for (ChessPiece p : board.getPieces(color))
            if (p.isRevealed() && p.getType() == ChessPiece.KING) { king = p; break; }
        if (king == null) return -100000;
        int score = 0;
        int kr = king.getRow(), kc = king.getCol();
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = kr + dr, c = kc + dc;
                if (r<0||r>9||c<0||c>8) continue;
                ChessPiece p = board.getPiece(r,c);
                if (p!=null && p.getColor()==color && p.isRevealed() &&
                    (p.getType()==ChessPiece.ADVISOR || p.getType()==ChessPiece.BISHOP))
                    score += 30;
            }
        if (color == ChessPiece.RED) score += (9 - kr) * 10;
        else score += kr * 10;
        if (RuleValidator.isInCheck(board, color)) score -= 150;
        return score;
    }

    private static int evaluateThreats(Board board, int color) {
        int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        int score = 0;
        Set<String> threatened = new HashSet<>();
        for (Move m : RuleValidator.generateAllMoves(board, oppColor)) {
            ChessPiece target = board.getPiece(m.getDestination());
            if (target != null && target.getColor() == color)
                threatened.add(m.getDestination());
        }
        for (Move m : RuleValidator.generateAllMoves(board, color)) {
            ChessPiece target = board.getPiece(m.getDestination());
            if (target != null && target.getColor() == oppColor)
                score += target.getValue() / 8;
        }
        for (String coord : threatened) {
            ChessPiece p = board.getPiece(coord);
            if (p != null) score -= p.getValue() / 10;
        }
        return score;
    }

    private static int evaluatePawnStructure(Board board, int color) {
        List<ChessPiece> pawns = new ArrayList<>();
        for (ChessPiece p : board.getPieces(color))
            if (p.isRevealed() && p.getType() == ChessPiece.PAWN) pawns.add(p);
        int score = 0;
        for (int i = 0; i < pawns.size(); i++)
            for (int j = i+1; j < pawns.size(); j++) {
                if (Math.abs(pawns.get(i).getCol() - pawns.get(j).getCol()) == 1 &&
                    Math.abs(pawns.get(i).getRow() - pawns.get(j).getRow()) <= 1)
                    score += 10;
            }
        return score;
    }
}
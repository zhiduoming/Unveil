package com.jieqi.core;

import java.util.*;

public class RuleValidator {

    public static boolean isValidMove(Board board, Move move, int currentColor) {
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        ChessPiece piece = board.getPiece(src[0], src[1]);
        if (piece == null) return false;
        if (piece.getColor() != currentColor) return false;
        ChessPiece target = board.getPiece(dst[0], dst[1]);
        if (target != null && target.getColor() == currentColor) return false;

        if (move.isFlipOnly() || (src[0] == dst[0] && src[1] == dst[1])) {
            return false;
        }

        int moveType = piece.getMoveType();
        switch (moveType) {
            case ChessPiece.KING:   return isValidKingMove(board, piece, src, dst);
            case ChessPiece.ROOK:   return isValidRookMove(board, piece, src, dst);
            case ChessPiece.KNIGHT: return isValidKnightMove(board, piece, src, dst);
            case ChessPiece.CANNON: return isValidCannonMove(board, piece, src, dst);
            case ChessPiece.PAWN:   return isValidPawnMove(board, piece, src, dst);
            case ChessPiece.ADVISOR:return isValidAdvisorMove(board, piece, src, dst);
            case ChessPiece.BISHOP: return isValidBishopMove(board, piece, src, dst);
            default: return false;
        }
    }

    private static boolean isValidKingMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        int dr = Math.abs(dst[0] - src[0]);
        int dc = Math.abs(dst[1] - src[1]);
        if (dr + dc != 1) return false;
        if (dst[1] < 3 || dst[1] > 5) return false;
        return piece.getColor() == ChessPiece.RED ? (dst[0] >= 7 && dst[0] <= 9) 
                                                   : (dst[0] >= 0 && dst[0] <= 2);
    }

    private static boolean isValidRookMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        if (src[0] != dst[0] && src[1] != dst[1]) return false;
        if (src[0] == dst[0]) {
            int minC = Math.min(src[1], dst[1]);
            int maxC = Math.max(src[1], dst[1]);
            for (int c = minC + 1; c < maxC; c++) {
                if (board.getPiece(src[0], c) != null) return false;
            }
        } else {
            int minR = Math.min(src[0], dst[0]);
            int maxR = Math.max(src[0], dst[0]);
            for (int r = minR + 1; r < maxR; r++) {
                if (board.getPiece(r, src[1]) != null) return false;
            }
        }
        return true;
    }

    private static boolean isValidKnightMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        int dr = dst[0] - src[0];
        int dc = dst[1] - src[1];
        if (Math.abs(dr) == 2 && Math.abs(dc) == 1) {
            int legR = src[0] + (dr > 0 ? 1 : -1);
            return board.getPiece(legR, src[1]) == null;
        } else if (Math.abs(dr) == 1 && Math.abs(dc) == 2) {
            int legC = src[1] + (dc > 0 ? 1 : -1);
            return board.getPiece(src[0], legC) == null;
        }
        return false;
    }

    private static boolean isValidCannonMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        if (src[0] != dst[0] && src[1] != dst[1]) return false;
        ChessPiece target = board.getPiece(dst[0], dst[1]);
        int count = 0;
        if (src[0] == dst[0]) {
            int minC = Math.min(src[1], dst[1]);
            int maxC = Math.max(src[1], dst[1]);
            for (int c = minC + 1; c < maxC; c++) {
                if (board.getPiece(src[0], c) != null) count++;
            }
        } else {
            int minR = Math.min(src[0], dst[0]);
            int maxR = Math.max(src[0], dst[0]);
            for (int r = minR + 1; r < maxR; r++) {
                if (board.getPiece(r, src[1]) != null) count++;
            }
        }
        if (target == null) return count == 0;
        else return count == 1;
    }

    private static boolean isValidPawnMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        int dr = dst[0] - src[0];
        int dc = Math.abs(dst[1] - src[1]);
        if (dc > 1 || Math.abs(dr) > 1) return false;
        if (dc == 1 && dr != 0) return false;
        int forward = (piece.getColor() == ChessPiece.RED) ? -1 : 1;
        boolean crossed = (piece.getColor() == ChessPiece.RED) ? src[0] <= 4 : src[0] >= 5;
        if (!crossed) return dr == forward && dc == 0;
        return (dr == forward && dc == 0) || (dr == 0 && dc == 1);
    }

    private static boolean isValidAdvisorMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        if (Math.abs(dst[0] - src[0]) != 1 || Math.abs(dst[1] - src[1]) != 1) {
            return false;
        }
        // 暗子按原始象棋规则：士限九宫；明士可全场斜走（强化）
        if (!piece.isRevealed()) {
            if (dst[1] < 3 || dst[1] > 5) {
                return false;
            }
            return piece.getColor() == ChessPiece.RED
                    ? (dst[0] >= 7 && dst[0] <= 9)
                    : (dst[0] >= 0 && dst[0] <= 2);
        }
        return true;
    }

    private static boolean isValidBishopMove(Board board, ChessPiece piece, int[] src, int[] dst) {
        int dr = Math.abs(dst[0] - src[0]);
        int dc = Math.abs(dst[1] - src[1]);
        if (dr != 2 || dc != 2) return false;
        int eyeR = (src[0] + dst[0]) / 2;
        int eyeC = (src[1] + dst[1]) / 2;
        if (board.getPiece(eyeR, eyeC) != null) {
            return false;
        }
        // 暗象不过河；明象可过河
        if (!piece.isRevealed()) {
            return piece.getColor() == ChessPiece.RED ? (dst[0] >= 5) : (dst[0] <= 4);
        }
        return true;
    }

    public static List<Move> generateAllMoves(Board board, int color) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece piece = board.getPiece(r, c);
                if (piece == null || piece.getColor() != color) continue;
                String src = ChessPiece.toCoord(r, c);
                for (int dr = 0; dr < 10; dr++) {
                    for (int dc = 0; dc < 9; dc++) {
                        if (dr == r && dc == c) continue;
                        Move move = new Move(src, ChessPiece.toCoord(dr, dc));
                        if (isValidMove(board, move, color)) moves.add(move);
                    }
                }
            }
        }
        return moves;
    }

    public static boolean isInCheck(Board board, int color) {
        ChessPiece king = null;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p != null && p.isRevealed() && p.getType() == ChessPiece.KING && p.getColor() == color) {
                    king = p;
                    break;
                }
            }
            if (king != null) break;
        }
        if (king == null) return true;
        int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        ChessPiece oppKing = null;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null || p.getColor() != oppColor) continue;
                if (p.isRevealed() && p.getType() == ChessPiece.KING) {
                    oppKing = p;
                }
                Move testMove = new Move(ChessPiece.toCoord(r, c), ChessPiece.toCoord(king.getRow(), king.getCol()));
                if (isValidMove(board, testMove, oppColor)) return true;
            }
        }
        if (oppKing != null && oppKing.getCol() == king.getCol()) {
            int minR = Math.min(king.getRow(), oppKing.getRow());
            int maxR = Math.max(king.getRow(), oppKing.getRow());
            boolean blocked = false;
            for (int r = minR + 1; r < maxR; r++) {
                if (board.getPiece(r, king.getCol()) != null) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) return true;
        }
        return false;
    }

    /**
     * 走子后己方是否仍被将军。服务器用本检查拒绝送将。
     */
    public static boolean isMoveLegal(Board board, Move move, int color) {
        Board simulated = new Board(board);
        simulated.executeMove(move);
        return !isInCheck(simulated, color);
    }

    public static boolean isCheckmate(Board board, int color) {
        if (!isInCheck(board, color)) return false;
        List<Move> moves = generateAllMoves(board, color);
        for (Move move : moves) {
            ChessPiece captured = board.executeMove(move);
            boolean stillInCheck = isInCheck(board, color);
            board.undoMove(move, captured);
            if (!stillInCheck) return false;
        }
        return true;
    }

    public static boolean isStalemate(Board board, int color) {
        if (isInCheck(board, color)) return false;
        List<Move> moves = generateAllMoves(board, color);
        if (moves.isEmpty()) return true;
        for (Move move : moves) {
            ChessPiece captured = board.executeMove(move);
            boolean inCheck = isInCheck(board, color);
            board.undoMove(move, captured);
            if (!inCheck) return false;
        }
        return true;
    }
}

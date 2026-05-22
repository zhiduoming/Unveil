package com.jieqi.core;

import java.util.*;

public class Board {
    private ChessPiece[][] grid;
    private List<ChessPiece> redPieces;
    private List<ChessPiece> blackPieces;
    private List<Move> moveHistory;
    private int moveCount;
    private int noCaptureCount;

    // 每个位置原本应有的棋子类型（用于暗子的virtualType）
    private static final int[][] POSITION_VIRTUAL_TYPES = {
        // row0: 黑方底线
        {ChessPiece.ROOK, ChessPiece.KNIGHT, ChessPiece.BISHOP, ChessPiece.ADVISOR, ChessPiece.KING, 
         ChessPiece.ADVISOR, ChessPiece.BISHOP, ChessPiece.KNIGHT, ChessPiece.ROOK},
        // row1: 空
        {ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN},
        // row2: 黑方炮行
        {ChessPiece.UNKNOWN, ChessPiece.CANNON, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.CANNON, ChessPiece.UNKNOWN},
        // row3: 黑方卒行
        {ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN,
         ChessPiece.UNKNOWN, ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN},
        // row4: 楚河汉界（空）
        {ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN},
        // row5: 楚河汉界（空）
        {ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN},
        // row6: 红方兵行
        {ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN,
         ChessPiece.UNKNOWN, ChessPiece.PAWN, ChessPiece.UNKNOWN, ChessPiece.PAWN},
        // row7: 红方炮行
        {ChessPiece.UNKNOWN, ChessPiece.CANNON, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.CANNON, ChessPiece.UNKNOWN},
        // row8: 空
        {ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN,
         ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN, ChessPiece.UNKNOWN},
        // row9: 红方底线
        {ChessPiece.ROOK, ChessPiece.KNIGHT, ChessPiece.BISHOP, ChessPiece.ADVISOR, ChessPiece.KING,
         ChessPiece.ADVISOR, ChessPiece.BISHOP, ChessPiece.KNIGHT, ChessPiece.ROOK}
    };

    private static final int[] DARK_PIECE_TYPES = {
        ChessPiece.ROOK, ChessPiece.ROOK,
        ChessPiece.KNIGHT, ChessPiece.KNIGHT,
        ChessPiece.CANNON, ChessPiece.CANNON,
        ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN, ChessPiece.PAWN,
        ChessPiece.ADVISOR, ChessPiece.ADVISOR,
        ChessPiece.BISHOP, ChessPiece.BISHOP
    };

    public Board() {
        grid = new ChessPiece[10][9];
        redPieces = new ArrayList<>();
        blackPieces = new ArrayList<>();
        moveHistory = new ArrayList<>();
        moveCount = 0;
        noCaptureCount = 0;
        initBoard();
    }

    public Board(Board other) {
        this.grid = new ChessPiece[10][9];
        this.redPieces = new ArrayList<>();
        this.blackPieces = new ArrayList<>();
        this.moveHistory = new ArrayList<>(other.moveHistory);
        this.moveCount = other.moveCount;
        this.noCaptureCount = other.noCaptureCount;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                if (other.grid[r][c] != null) {
                    this.grid[r][c] = new ChessPiece(other.grid[r][c]);
                    if (this.grid[r][c].getColor() == ChessPiece.RED) {
                        redPieces.add(this.grid[r][c]);
                    } else {
                        blackPieces.add(this.grid[r][c]);
                    }
                }
            }
        }
    }

    private void initBoard() {
        // 将帅
        grid[0][4] = new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4);
        grid[0][4].setVirtualType(ChessPiece.KING);
        blackPieces.add(grid[0][4]);

        grid[9][4] = new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4);
        grid[9][4].setVirtualType(ChessPiece.KING);
        redPieces.add(grid[9][4]);

        // 收集暗子位置
        List<int[]> redDarkPositions = new ArrayList<>();
        List<int[]> blackDarkPositions = new ArrayList<>();

        for (int r = 6; r <= 9; r++) {
            for (int c = 0; c < 9; c++) {
                int vt = POSITION_VIRTUAL_TYPES[r][c];
                if (vt != ChessPiece.UNKNOWN && !(r == 9 && c == 4)) {
                    redDarkPositions.add(new int[]{r, c, vt});
                }
            }
        }
        for (int r = 0; r <= 3; r++) {
            for (int c = 0; c < 9; c++) {
                int vt = POSITION_VIRTUAL_TYPES[r][c];
                if (vt != ChessPiece.UNKNOWN && !(r == 0 && c == 4)) {
                    blackDarkPositions.add(new int[]{r, c, vt});
                }
            }
        }

        List<Integer> redTypes = shuffleArray(DARK_PIECE_TYPES);
        List<Integer> blackTypes = shuffleArray(DARK_PIECE_TYPES);

        for (int i = 0; i < 15; i++) {
            int[] posR = redDarkPositions.get(i);
            ChessPiece rp = new ChessPiece(redTypes.get(i), ChessPiece.RED, false, posR[0], posR[1]);
            rp.setVirtualType(posR[2]);
            grid[posR[0]][posR[1]] = rp;
            redPieces.add(rp);

            int[] posB = blackDarkPositions.get(i);
            ChessPiece bp = new ChessPiece(blackTypes.get(i), ChessPiece.BLACK, false, posB[0], posB[1]);
            bp.setVirtualType(posB[2]);
            grid[posB[0]][posB[1]] = bp;
            blackPieces.add(bp);
        }
    }

    private List<Integer> shuffleArray(int[] arr) {
        List<Integer> list = new ArrayList<>();
        for (int v : arr) list.add(v);
        Collections.shuffle(list);
        return list;
    }

    public ChessPiece getPiece(int row, int col) {
        if (row < 0 || row > 9 || col < 0 || col > 8) return null;
        return grid[row][col];
    }

    public ChessPiece getPiece(String coord) {
        int[] rc = ChessPiece.fromCoord(coord);
        return getPiece(rc[0], rc[1]);
    }

    /**
     * 执行走法（正确处理翻子、移动、吃子）
     */
    public ChessPiece executeMove(Move move) {
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());

        // 1. 翻子操作（原地翻开）
        if (move.isFlipOnly()) {
            ChessPiece piece = grid[src[0]][src[1]];
            if (piece == null || piece.isRevealed()) {
                return null; // 非法，调用前应已校验
            }
            piece.setRevealed(true);
            move.setType(piece.getType());
            moveHistory.add(move);
            moveCount++;
            noCaptureCount++; // 翻子不算吃子，增加无吃子计数
            return null;
        }

        // 2. 正常移动（可能吃子）
        ChessPiece piece = grid[src[0]][src[1]];
        ChessPiece captured = grid[dst[0]][dst[1]];

        if (piece == null) return null;

        moveHistory.add(move);
        moveCount++;

        if (captured != null) {
            if (captured.getColor() == ChessPiece.RED) redPieces.remove(captured);
            else blackPieces.remove(captured);
            noCaptureCount = 0;
        } else {
            noCaptureCount++;
        }

        grid[dst[0]][dst[1]] = piece;
        grid[src[0]][src[1]] = null;
        piece.setRow(dst[0]);
        piece.setCol(dst[1]);

        // 移动暗子后必须翻开
        if (!piece.isRevealed()) {
            piece.setRevealed(true);
            move.setType(piece.getType());
        }

        return captured;
    }

    /**
     * 撤销走法（悔棋）
     * @param move 要撤销的走法
     * @param captured 该走法吃掉的棋子（如果无吃子则为null）
     */
    public void undoMove(Move move, ChessPiece captured) {
        // 翻子操作的撤销
        if (move.isFlipOnly()) {
            int[] pos = ChessPiece.fromCoord(move.getSource());
            ChessPiece piece = grid[pos[0]][pos[1]];
            if (piece != null && piece.isRevealed()) {
                piece.setRevealed(false);
            }
            moveHistory.remove(moveHistory.size() - 1);
            moveCount--;
            if (noCaptureCount > 0) noCaptureCount--;
            return;
        }

        // 正常移动的撤销
        int[] src = ChessPiece.fromCoord(move.getSource());
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        ChessPiece piece = grid[dst[0]][dst[1]];

        if (piece == null) {
            System.err.println("undoMove error: piece is null at " + move.getDestination());
            return;
        }

        // 如果这一步翻开了棋子（移动暗子后翻开），恢复为暗子
        if (move.getType() != null && !move.isFlipOnly()) {
            piece.setRevealed(false);
        }

        grid[src[0]][src[1]] = piece;
        grid[dst[0]][dst[1]] = captured;
        piece.setRow(src[0]);
        piece.setCol(src[1]);

        if (captured != null) {
            if (captured.getColor() == ChessPiece.RED) redPieces.add(captured);
            else blackPieces.add(captured);
            // 撤销吃子后，无吃子计数需要回退，但准确值较复杂，这里简单重置为0（最安全）
            noCaptureCount = 0;
        } else {
            if (noCaptureCount > 0) noCaptureCount--;
        }

        moveHistory.remove(moveHistory.size() - 1);
        moveCount--;
    }

    public List<ChessPiece> getPieces(int color) {
        return color == ChessPiece.RED ? redPieces : blackPieces;
    }

    public List<ChessPiece> getUnrevealedPieces(int color) {
        List<ChessPiece> result = new ArrayList<>();
        for (ChessPiece p : getPieces(color)) {
            if (!p.isRevealed()) result.add(p);
        }
        return result;
    }

    public int getExpectedValue(int color) {
        List<ChessPiece> unrevealed = getUnrevealedPieces(color);
        if (unrevealed.isEmpty()) return 0;
        int total = 0;
        for (ChessPiece p : unrevealed) {
            total += ChessPiece.getBaseValue(p.getVirtualType());
        }
        return total / unrevealed.size();
    }

    public boolean hasKing(int color) {
        for (ChessPiece p : getPieces(color)) {
            if (p.isRevealed() && p.getType() == ChessPiece.KING) return true;
        }
        return false;
    }

    public int getNoCaptureCount() { return noCaptureCount; }
    public List<Move> getMoveHistory() { return new ArrayList<>(moveHistory); }
    public int getMoveCount() { return moveCount; }
    public ChessPiece[][] getGrid() { return grid; }

    public String getGameRecord() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            Move m = moveHistory.get(i);
            sb.append((i + 1)).append(". ");
            sb.append(m.getSource()).append("-").append(m.getDestination());
            if (m.getType() != null) {
                sb.append("(").append(ChessPiece.getTypeName(m.getType(), i % 2 == 0 ? ChessPiece.RED : ChessPiece.BLACK)).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
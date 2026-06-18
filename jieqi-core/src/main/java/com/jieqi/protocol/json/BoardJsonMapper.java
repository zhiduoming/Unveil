package com.jieqi.protocol.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Coordinate;

/** Board ↔ 老师 initialBoard JSON 数组。 */
public final class BoardJsonMapper {

    private BoardJsonMapper() {}

    public static JsonArray toInitialBoard(Board board) {
        return toBoardCells(board, false);
    }

    /** 复盘用棋盘 JSON：暗子可选携带真实类型（仅终局复盘应开启）。 */
    public static JsonArray toReplayBoard(Board board) {
        return toReplayBoard(board, true);
    }

    public static JsonArray toReplayBoard(Board board, boolean revealRealPieces) {
        return toBoardCells(board, revealRealPieces);
    }

    private static JsonArray toBoardCells(Board board, boolean revealRealPieces) {
        JsonArray cells = new JsonArray();
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ChessPiece p = board.getPiece(r, c);
                if (p == null) {
                    continue;
                }
                JsonObject cell = new JsonObject();
                cell.addProperty("x", String.valueOf((char) ('a' + c)));
                cell.addProperty("y", 9 - r);
                cell.addProperty("color", PieceJsonMapper.colorToString(p.getColor()));
                if (p.isRevealed()) {
                    cell.addProperty("piece", PieceJsonMapper.toJsonName(p.getType()));
                    cell.addProperty("visible", true);
                } else {
                    cell.addProperty("piece", PieceJsonMapper.toJsonName(p.getVirtualType()));
                    cell.addProperty("visible", false);
                    if (revealRealPieces) {
                        cell.addProperty("realPiece", PieceJsonMapper.toJsonName(p.getType()));
                        cell.addProperty("virtualPiece", PieceJsonMapper.toJsonName(p.getVirtualType()));
                    }
                }
                cells.add(cell);
            }
        }
        return cells;
    }

    /** 从复盘帧 board JSON 重建棋盘（不触发随机暗子初始化）。 */
    public static Board fromBoardJson(JsonArray cells) {
        Board board = new Board();
        board.clearAllPieces();
        if (cells == null) {
            return board;
        }
        for (int i = 0; i < cells.size(); i++) {
            JsonObject cell = cells.get(i).getAsJsonObject();
            String x = cell.get("x").getAsString();
            int y = cell.get("y").getAsInt();
            int[] rc = Coordinate.toRowCol(x + y);
            int row = rc[0];
            int col = rc[1];
            int color = PieceJsonMapper.colorFromString(cell.get("color").getAsString());
            boolean visible = cell.has("visible") && cell.get("visible").getAsBoolean();
            int type;
            int virtualType = ChessPiece.UNKNOWN;
            if (visible) {
                type = PieceJsonMapper.fromJsonName(cell.get("piece").getAsString());
            } else if (cell.has("realPiece")) {
                type = PieceJsonMapper.fromJsonName(cell.get("realPiece").getAsString());
                if (cell.has("virtualPiece")) {
                    virtualType = PieceJsonMapper.fromJsonName(cell.get("virtualPiece").getAsString());
                } else {
                    virtualType = PieceJsonMapper.fromJsonName(cell.get("piece").getAsString());
                }
            } else {
                type = PieceJsonMapper.fromJsonName(cell.get("piece").getAsString());
                virtualType = type;
            }
            ChessPiece piece = new ChessPiece(type, color, visible, row, col);
            if (!visible) {
                piece.setVirtualType(virtualType);
            } else {
                piece.setVirtualType(type);
            }
            board.placePiece(piece, row, col);
        }
        return board;
    }

    /** 从 gameStart 的 initialBoard 完整重建客户端棋盘（rematch 安全）。 */
    public static Board fromInitialBoard(JsonArray cells) {
        Board board = new Board();
        board.clearAllPieces();
        if (cells == null) {
            return board;
        }
        for (int i = 0; i < cells.size(); i++) {
            JsonObject cell = cells.get(i).getAsJsonObject();
            String x = cell.get("x").getAsString();
            int y = cell.get("y").getAsInt();
            int[] rc = Coordinate.toRowCol(x + y);
            int row = rc[0];
            int col = rc[1];
            int color = PieceJsonMapper.colorFromString(cell.get("color").getAsString());
            boolean visible = cell.has("visible") && cell.get("visible").getAsBoolean();
            int pieceType = PieceJsonMapper.fromJsonName(cell.get("piece").getAsString());
            ChessPiece piece = new ChessPiece(pieceType, color, visible, row, col);
            if (!visible) {
                piece.setVirtualType(pieceType);
            } else {
                piece.setVirtualType(pieceType);
            }
            board.placePiece(piece, row, col);
        }
        return board;
    }

    /** 从 initialBoard 更新已有棋盘上的明子信息（用于 WebSocket 客户端本地显示）。 */
    public static void applyInitialBoard(Board board, JsonArray cells) {
        if (cells == null) {
            return;
        }
        for (int i = 0; i < cells.size(); i++) {
            JsonObject cell = cells.get(i).getAsJsonObject();
            String x = cell.get("x").getAsString();
            int y = cell.get("y").getAsInt();
            boolean visible = cell.has("visible") && cell.get("visible").getAsBoolean();
            int type = PieceJsonMapper.fromJsonName(cell.get("piece").getAsString());
            String coord = x + y;
            int[] rc = Coordinate.toRowCol(coord);
            ChessPiece piece = board.getPiece(rc[0], rc[1]);
            if (piece == null) {
                continue;
            }
            if (visible) {
                piece.setRevealed(true);
                piece.setType(type);
            }
        }
    }
}

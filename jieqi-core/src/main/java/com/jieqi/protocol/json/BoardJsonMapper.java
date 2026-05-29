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
                if (p.isRevealed()) {
                    cell.addProperty("piece", PieceJsonMapper.toJsonName(p.getType()));
                    cell.addProperty("visible", true);
                } else {
                    cell.addProperty("piece", PieceJsonMapper.toJsonName(p.getVirtualType()));
                    cell.addProperty("visible", false);
                }
                cells.add(cell);
            }
        }
        return cells;
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

package com.jieqi.protocol.json;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardJsonMapperTest {

    @Test
    void initialBoardContainsKingsAndHiddenPieces() {
        Board board = new Board();
        var cells = BoardJsonMapper.toInitialBoard(board);
        assertTrue(cells.size() >= 32);
        boolean hasVisibleKing = false;
        boolean hasHidden = false;
        for (int i = 0; i < cells.size(); i++) {
            var cell = cells.get(i).getAsJsonObject();
            if (cell.get("visible").getAsBoolean() && "king".equals(cell.get("piece").getAsString())) {
                hasVisibleKing = true;
            }
            if (!cell.get("visible").getAsBoolean()) {
                hasHidden = true;
            }
        }
        assertTrue(hasVisibleKing);
        assertTrue(hasHidden);
    }

    @Test
    void pieceNameRoundTrip() {
        assertEquals("cannon", PieceJsonMapper.toJsonName(ChessPiece.CANNON));
        assertEquals(ChessPiece.CANNON, PieceJsonMapper.fromJsonName("Cannon"));
    }

    @Test
    void initialBoardKeepsActualColorForPiecesAcrossRiver() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 2, 4), 2, 4);
        board.placePiece(new ChessPiece(ChessPiece.CANNON, ChessPiece.BLACK, true, 7, 1), 7, 1);

        var cells = BoardJsonMapper.toInitialBoard(board);

        assertTrue(containsCell(cells, "e", 7, "rook", "red"));
        assertTrue(containsCell(cells, "b", 2, "cannon", "black"));
    }

    private static boolean containsCell(Iterable<com.google.gson.JsonElement> cells,
                                        String x,
                                        int y,
                                        String piece,
                                        String color) {
        for (var element : cells) {
            var cell = element.getAsJsonObject();
            if (x.equals(cell.get("x").getAsString())
                    && y == cell.get("y").getAsInt()
                    && piece.equals(cell.get("piece").getAsString())
                    && color.equals(cell.get("color").getAsString())) {
                return true;
            }
        }
        return false;
    }
}

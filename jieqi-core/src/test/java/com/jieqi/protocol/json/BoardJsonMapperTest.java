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
}

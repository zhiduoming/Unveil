package com.jieqi.protocol.json;

import com.google.gson.JsonArray;
import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardJsonMapperReplayTest {

    @Test
    void toReplayBoard_includesRealAndVirtualPieceForDark() {
        Board board = new Board();
        board.clearAllPieces();
        ChessPiece dark = new ChessPiece(ChessPiece.KNIGHT, ChessPiece.BLACK, false, 0, 0);
        dark.setVirtualType(ChessPiece.ROOK);
        board.placePiece(dark, 0, 0);

        JsonArray cells = BoardJsonMapper.toReplayBoard(board);
        assertEquals(1, cells.size());
        var cell = cells.get(0).getAsJsonObject();
        assertFalse(cell.get("visible").getAsBoolean());
        assertEquals("knight", cell.get("realPiece").getAsString());
        assertEquals("rook", cell.get("virtualPiece").getAsString());
    }

    @Test
    void fromBoardJson_roundTripPreservesDarkIdentity() {
        Board board = new Board();
        board.clearAllPieces();
        ChessPiece dark = new ChessPiece(ChessPiece.CANNON, ChessPiece.RED, false, 9, 0);
        dark.setVirtualType(ChessPiece.PAWN);
        board.placePiece(dark, 9, 0);

        JsonArray cells = BoardJsonMapper.toReplayBoard(board);
        Board rebuilt = BoardJsonMapper.fromBoardJson(cells);
        ChessPiece p = rebuilt.getPiece(9, 0);
        assertNotNull(p);
        assertEquals(ChessPiece.CANNON, p.getType());
        assertEquals(ChessPiece.PAWN, p.getVirtualType());
        assertFalse(p.isRevealed());
    }
}

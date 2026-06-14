package com.jieqi.protocol.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 揭棋信息差：moveResult.captured 按接收方视角脱敏。
 * 吃子方/观战者看真实身份；被吃方在暗子被吃时看不到身份（INTERFACE.typ Q1 方案 B）。
 */
class JsonMessagesCapturedTest {

    private static ChessPiece piece(int type, int color, boolean revealed) {
        return new ChessPiece(type, color, revealed, 0, 0);
    }

    @Test
    void nullCapturedReturnsNull() {
        assertNull(JsonMessages.capturedJson(null, ChessPiece.RED));
        assertNull(JsonMessages.capturedJson(null, -1));
    }

    @Test
    void revealedPieceVisibleToEveryone() {
        ChessPiece blackRook = piece(ChessPiece.ROOK, ChessPiece.BLACK, true);
        for (int viewer : new int[]{ChessPiece.RED, ChessPiece.BLACK, -1}) {
            JsonObject c = JsonMessages.capturedJson(blackRook, viewer);
            assertEquals("black", c.get("color").getAsString());
            assertFalse(c.get("wasDark").getAsBoolean());
            assertTrue(c.has("piece"), "明子被吃双方都应可见身份, viewer=" + viewer);
            assertEquals("rook", c.get("piece").getAsString());
        }
    }

    @Test
    void darkPieceHiddenOnlyFromItsOwner() {
        ChessPiece darkBlackCannon = piece(ChessPiece.CANNON, ChessPiece.BLACK, false);

        // 吃子方（红）可见真实身份
        JsonObject toRed = JsonMessages.capturedJson(darkBlackCannon, ChessPiece.RED);
        assertTrue(toRed.has("piece"));
        assertEquals("cannon", toRed.get("piece").getAsString());
        assertTrue(toRed.get("wasDark").getAsBoolean());

        // 被吃方（黑，暗子的拥有者）看不到真实身份，只知 wasDark
        JsonObject toBlack = JsonMessages.capturedJson(darkBlackCannon, ChessPiece.BLACK);
        assertFalse(toBlack.has("piece"), "被吃方不应看到自己被吃暗子的真实身份");
        assertTrue(toBlack.get("wasDark").getAsBoolean());
        assertEquals("black", toBlack.get("color").getAsString());

        // 观战者（-1）上帝视角可见
        JsonObject toObs = JsonMessages.capturedJson(darkBlackCannon, -1);
        assertTrue(toObs.has("piece"));
        assertEquals("cannon", toObs.get("piece").getAsString());
    }

    @Test
    void darkRedPieceHiddenFromRedOwnerVisibleToBlackCaptor() {
        ChessPiece darkRedPawn = piece(ChessPiece.PAWN, ChessPiece.RED, false);

        assertFalse(JsonMessages.capturedJson(darkRedPawn, ChessPiece.RED).has("piece"));
        assertEquals("pawn",
                JsonMessages.capturedJson(darkRedPawn, ChessPiece.BLACK).get("piece").getAsString());
        assertEquals("pawn",
                JsonMessages.capturedJson(darkRedPawn, -1).get("piece").getAsString());
    }

    @Test
    void capturedRevealAlwaysCarriesTrueIdentity() {
        JsonArray arr = JsonMessages.capturedReveal(List.of(
                piece(ChessPiece.ROOK, ChessPiece.BLACK, true),
                piece(ChessPiece.PAWN, ChessPiece.RED, false) // 暗子也揭晓
        ));
        assertEquals(2, arr.size());
        JsonObject first = arr.get(0).getAsJsonObject();
        assertEquals("black", first.get("color").getAsString());
        assertEquals("rook", first.get("piece").getAsString());
        assertFalse(first.get("wasDark").getAsBoolean());

        JsonObject second = arr.get(1).getAsJsonObject();
        assertEquals("red", second.get("color").getAsString());
        assertEquals("pawn", second.get("piece").getAsString());
        assertTrue(second.get("wasDark").getAsBoolean(), "终局应揭晓暗子且标记 wasDark");
    }
}

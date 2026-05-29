package com.jieqi.protocol.json;

import com.jieqi.core.ChessPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PieceJsonMapperTest {

    private static final String[] TEACHER_NAMES = {
            "king", "rook", "knight", "cannon", "pawn", "guard", "bishop"
    };

    @Test
    void toJsonNameMatchesTeacherSpec() {
        assertEquals("king", PieceJsonMapper.toJsonName(ChessPiece.KING));
        assertEquals("rook", PieceJsonMapper.toJsonName(ChessPiece.ROOK));
        assertEquals("knight", PieceJsonMapper.toJsonName(ChessPiece.KNIGHT));
        assertEquals("cannon", PieceJsonMapper.toJsonName(ChessPiece.CANNON));
        assertEquals("pawn", PieceJsonMapper.toJsonName(ChessPiece.PAWN));
        assertEquals("guard", PieceJsonMapper.toJsonName(ChessPiece.ADVISOR));
        assertEquals("bishop", PieceJsonMapper.toJsonName(ChessPiece.BISHOP));
    }

    @Test
    void fromJsonNameParsesAllTeacherValues() {
        for (int i = 0; i < TEACHER_NAMES.length; i++) {
            assertEquals(i, PieceJsonMapper.fromJsonName(TEACHER_NAMES[i]));
            assertEquals(i, PieceJsonMapper.fromJsonName(TEACHER_NAMES[i].toUpperCase()));
        }
    }

    @Test
    void roundTripAllTypes() {
        for (int type = 0; type <= ChessPiece.BISHOP; type++) {
            String name = PieceJsonMapper.toJsonName(type);
            assertEquals(type, PieceJsonMapper.fromJsonName(name));
        }
    }

    @Test
    void colorMapping() {
        assertEquals("red", PieceJsonMapper.colorToString(ChessPiece.RED));
        assertEquals("black", PieceJsonMapper.colorToString(ChessPiece.BLACK));
        assertEquals(ChessPiece.RED, PieceJsonMapper.colorFromString("red"));
        assertEquals(ChessPiece.BLACK, PieceJsonMapper.colorFromString("black"));
    }
}

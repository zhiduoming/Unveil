package com.jieqi.ai.bot;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiBotFactoryTest {

    @Test
    void easyBotReturnsLegalMove() {
        Board board = new Board();
        AiBot bot = AiBotFactory.create(AiLevel.EASY, 500L);
        Move move = AiBotFactory.selectWithFallback(bot, board, ChessPiece.RED, 500L, null);
        assertNotNull(move);
        assertTrue(RuleValidator.isMoveLegal(board, move, ChessPiece.RED));
    }

    @Test
    void mediumBotReturnsLegalMove() {
        Board board = new Board();
        AiBot bot = AiBotFactory.create(AiLevel.MEDIUM, 800L);
        Move move = AiBotFactory.selectWithFallback(bot, board, ChessPiece.RED, 800L, null);
        assertNotNull(move);
        assertTrue(RuleValidator.isMoveLegal(board, move, ChessPiece.RED));
    }

    @Test
    void hardBotReturnsLegalMove() {
        Board board = new Board();
        AiBot bot = AiBotFactory.create(AiLevel.HARD, 1200L);
        Move move = AiBotFactory.selectWithFallback(bot, board, ChessPiece.RED, 1200L, null);
        assertNotNull(move);
        assertTrue(RuleValidator.isMoveLegal(board, move, ChessPiece.RED));
    }
}

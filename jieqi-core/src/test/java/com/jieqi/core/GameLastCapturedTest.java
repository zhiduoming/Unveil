package com.jieqi.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Game 被吃棋子记录（揭棋信息差所需）：
 * 吃明子 → lastCaptured.isRevealed()==true；吃暗子 → ==false 且 getType() 为真实身份；
 * capturedPieces 累积；无吃子时 lastCaptured 为 null。
 */
class GameLastCapturedTest {

    /** 红车吃掉黑方明子，记录其真实身份。 */
    @Test
    void capturingRevealedPieceRecordsTrueIdentity() {
        Game game = playingGame();
        Board board = game.getBoard();
        // 黑方明车（已揭示）在 b5（内部 (4,1)）
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.BLACK, true, 4, 1), 4, 1);

        String err = game.processMove(new Move("e5", "b5"), ChessPiece.RED);
        assertNull(err);

        ChessPiece captured = game.getLastCaptured();
        assertNotNull(captured);
        assertTrue(captured.isRevealed(), "明子被吃应为已揭示");
        assertEquals(ChessPiece.ROOK, captured.getType());
        assertEquals(ChessPiece.BLACK, captured.getColor());
        assertEquals(1, game.getCapturedPieces().size());
    }

    /** 红车吃掉黑方暗子：lastCaptured 未揭示，但真实身份可读。 */
    @Test
    void capturingDarkPieceKeepsHiddenButTrueTypeReadable() {
        Game game = playingGame();
        Board board = game.getBoard();
        // 黑方暗子（真实为炮）在 b5
        ChessPiece darkCannon = new ChessPiece(ChessPiece.CANNON, ChessPiece.BLACK, false, 4, 1);
        darkCannon.setVirtualType(ChessPiece.ROOK); // 任意虚拟身份，不影响被吃
        board.placePiece(darkCannon, 4, 1);

        String err = game.processMove(new Move("e5", "b5"), ChessPiece.RED);
        assertNull(err);

        ChessPiece captured = game.getLastCaptured();
        assertNotNull(captured);
        assertFalse(captured.isRevealed(), "暗子被吃前应保持未揭示");
        assertEquals(ChessPiece.CANNON, captured.getType(), "服务器侧应能读到真实身份");
        assertEquals(1, game.getCapturedPieces().size());
    }

    /** 无吃子的走子后 lastCaptured 应为 null，capturedPieces 为空。 */
    @Test
    void nonCapturingMoveLeavesNoCapture() {
        Game game = playingGame();

        String err = game.processMove(new Move("e5", "e6"), ChessPiece.RED);
        assertNull(err);

        assertNull(game.getLastCaptured());
        assertTrue(game.getCapturedPieces().isEmpty());
    }

    /**
     * 构造一个进行中的对局：红/黑帅分列两侧（避免飞将），红车在 e5。
     */
    private static Game playingGame() {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 0), 9, 0);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 8), 0, 8);
        board.placePiece(new ChessPiece(ChessPiece.ROOK, ChessPiece.RED, true, 4, 4), 4, 4);

        Game game = new Game("captured-test");
        game.connectPlayer(ChessPiece.RED);
        game.connectPlayer(ChessPiece.BLACK);
        game.replaceBoard(board);
        game.setCurrentTurn(ChessPiece.RED);
        return game;
    }
}

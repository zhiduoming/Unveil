package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 防透视：对手暗子真实身份不同，但 AI 公开展示棋盘与评估应一致。
 */
class AiFairnessTest {

    @Test
    void publicViewHidesOpponentHiddenIdentityFromEvaluation() {
        Board rookHidden = hiddenOpponentBoard(ChessPiece.ROOK, ChessPiece.ROOK);
        Board cannonHidden = hiddenOpponentBoard(ChessPiece.CANNON, ChessPiece.ROOK);

        Board viewRook = rookHidden.createAiPublicView(ChessPiece.RED);
        Board viewCannon = cannonHidden.createAiPublicView(ChessPiece.RED);

        assertEquals(ChessPiece.UNKNOWN, viewRook.getPiece(0, 0).getType());
        assertEquals(ChessPiece.UNKNOWN, viewCannon.getPiece(0, 0).getType());

        int scoreRook = EnhancedEvaluator.evaluate(viewRook, ChessPiece.RED);
        int scoreCannon = EnhancedEvaluator.evaluate(viewCannon, ChessPiece.RED);
        assertEquals(scoreRook, scoreCannon,
                "对手暗子真实身份不同不应影响脱敏棋盘评估");
    }

    @Test
    void agentMoveIsLegalOnAuthoritativeBoard() {
        Board board = new Board();
        JieqiAgent agent = new JieqiAgent();
        Move move = agent.selectMove(board, ChessPiece.RED, 500L);
        assertNotNull(move);
        assertTrue(com.jieqi.core.RuleValidator.isMoveLegal(board, move, ChessPiece.RED));
    }

    private static Board hiddenOpponentBoard(int trueType, int virtualType) {
        Board board = new Board();
        board.clearAllPieces();
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.RED, true, 9, 4), 9, 4);
        board.placePiece(new ChessPiece(ChessPiece.KING, ChessPiece.BLACK, true, 0, 4), 0, 4);
        ChessPiece hidden = new ChessPiece(trueType, ChessPiece.BLACK, false, 0, 0);
        hidden.setVirtualType(virtualType);
        board.placePiece(hidden, 0, 0);
        return board;
    }
}

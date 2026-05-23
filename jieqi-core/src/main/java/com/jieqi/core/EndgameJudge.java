package com.jieqi.core;

import java.util.Map;

/**
 * 终局判定：将死、困毙、无吃子和、重复局面等（与 {@link Game} 状态机配合）。
 */
public final class EndgameJudge {

    private EndgameJudge() {}

    public record Verdict(Game.GameStatus status, int reasonCode) {}

    /**
     * 走子后检查是否终局；若继续对弈返回 {@code null}。
     */
    public static Verdict checkAfterMove(
            Board board,
            int playerColor,
            ChessPiece captured,
            Map<String, Integer> repetitionCount,
            String boardHash) {

        int oppColor = (playerColor == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;

        if (captured != null && captured.isRevealed() && captured.getType() == ChessPiece.KING) {
            return win(playerColor, ProtocolReason.KING_CAPTURED);
        }
        if (RuleValidator.isCheckmate(board, oppColor)) {
            return win(playerColor, ProtocolReason.CHECKMATE);
        }
        if (RuleValidator.isStalemate(board, oppColor)) {
            return win(playerColor, ProtocolReason.STALEMATE);
        }
        if (board.getNoCaptureCount() >= 80) {
            return new Verdict(Game.GameStatus.DRAW, ProtocolReason.NO_CAPTURE_DRAW);
        }

        repetitionCount.put(boardHash, repetitionCount.getOrDefault(boardHash, 0) + 1);
        if (repetitionCount.get(boardHash) >= 6) {
            return new Verdict(Game.GameStatus.DRAW, ProtocolReason.REPETITION_DRAW);
        }
        return null;
    }

    private static Verdict win(int winnerColor, int reason) {
        Game.GameStatus status = (winnerColor == ChessPiece.RED)
                ? Game.GameStatus.RED_WIN
                : Game.GameStatus.BLACK_WIN;
        return new Verdict(status, reason);
    }

    /** 与 {@link com.jieqi.protocol.Protocol} 原因码对齐，避免 core 依赖 protocol 包。 */
    public static final class ProtocolReason {
        public static final int CHECKMATE = 0;
        public static final int STALEMATE = 1;
        public static final int KING_CAPTURED = 5;
        public static final int NO_CAPTURE_DRAW = 6;
        public static final int REPETITION_DRAW = 8;

        private ProtocolReason() {}
    }
}

package com.jieqi.core;

import java.util.Map;

/**
 * 终局判定：将死、困毙、无吃子和、长将/长捉/兵卒长捉等（与 {@link Game} 状态机配合）。
 */
public final class EndgameJudge {

    private EndgameJudge() {}

    public record Verdict(Game.GameStatus status, int reasonCode) {}

    /**
     * 走子后检查是否终局；若继续对弈返回 {@code null}。
     *
     * @param lastMove 刚执行的着法（用于长捉判定；可为 {@code null}）
     */
    public static Verdict checkAfterMove(
            Board board,
            int playerColor,
            ChessPiece captured,
            Map<String, Integer> repetitionCount,
            String boardHash,
            Move lastMove) {

        if (captured != null) {
            repetitionCount.clear();
        }

        int oppColor = opponent(playerColor);

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

        boolean givesCheck = RuleValidator.isInCheck(board, oppColor);
        boolean pawnChase = false;
        boolean chase = false;
        if (lastMove != null && !givesCheck) {
            chase = findChaseTarget(board, lastMove, playerColor) != null;
            pawnChase = chase && isPawnMover(board, lastMove);
        }

        int count = repetitionCount.getOrDefault(boardHash, 0) + 1;
        repetitionCount.put(boardHash, count);

        if (count >= 6) {
            if (givesCheck) {
                return lossFor(playerColor, ProtocolReason.REPETITION_LOSS);
            }
            if (pawnChase) {
                return new Verdict(Game.GameStatus.DRAW, ProtocolReason.REPETITION_DRAW);
            }
            if (chase) {
                return lossFor(playerColor, ProtocolReason.REPETITION_LOSS);
            }
        }
        return null;
    }

    /** 将军方（走子方）判负，对方胜。 */
    private static Verdict lossFor(int loserColor, int reason) {
        return win(opponent(loserColor), reason);
    }

    private static Verdict win(int winnerColor, int reason) {
        Game.GameStatus status = (winnerColor == ChessPiece.RED)
                ? Game.GameStatus.RED_WIN
                : Game.GameStatus.BLACK_WIN;
        return new Verdict(status, reason);
    }

    private static int opponent(int color) {
        return color == ChessPiece.RED ? ChessPiece.BLACK : ChessPiece.RED;
    }

    /**
     * 若走子方在 destination 的棋子可合法吃某一对方子，视为「捉」。
     *
     * @return 被捉子坐标，否则 {@code null}
     */
    static String findChaseTarget(Board board, Move move, int playerColor) {
        ChessPiece mover = board.getPiece(move.getDestination());
        if (mover == null || mover.getColor() != playerColor) {
            return null;
        }
        int oppColor = opponent(playerColor);
        for (ChessPiece target : board.getPieces(oppColor)) {
            if (target.getType() == ChessPiece.KING) {
                continue;
            }
            String targetCoord = ChessPiece.toCoord(target.getRow(), target.getCol());
            Move capture = new Move(move.getDestination(), targetCoord);
            if (RuleValidator.isValidMove(board, capture, playerColor)) {
                return targetCoord;
            }
        }
        return null;
    }

    static boolean isPawnMover(Board board, Move move) {
        ChessPiece mover = board.getPiece(move.getDestination());
        if (mover == null) {
            return false;
        }
        return mover.getMoveType() == ChessPiece.PAWN;
    }

    /** 与 {@link com.jieqi.protocol.Protocol} 原因码对齐，避免 core 依赖 protocol 包。 */
    public static final class ProtocolReason {
        public static final int CHECKMATE = 0;
        public static final int STALEMATE = 1;
        public static final int TIMEOUT = 2;
        public static final int KING_CAPTURED = 5;
        public static final int NO_CAPTURE_DRAW = 6;
        public static final int REPETITION_LOSS = 7;
        public static final int REPETITION_DRAW = 8;

        private ProtocolReason() {}
    }
}

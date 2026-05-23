package com.jieqi.core;

/**
 * 服务器权威翻子：开局随机类型已在 {@link Board} 初始化时确定，
 * 本服务负责忽略客户端伪造的 type，并在走子后以棋盘真实类型写回 {@link Move}。
 */
public final class RandomRevealService {

    /** 清除客户端上传的 type（防作弊）。 */
    public void sanitizeClientMove(Move move) {
        if (move != null) {
            move.setType(null);
        }
    }

    /**
     * 走子执行后，若本步翻开棋子，用棋盘上的真实类型覆盖 Move.type。
     */
    public void stampServerRevealType(Move move, Board board) {
        if (move == null || board == null) {
            return;
        }
        ChessPiece piece = move.isFlipOnly()
                ? board.getPiece(move.getSource())
                : board.getPiece(move.getDestination());
        if (piece != null && piece.isRevealed()) {
            move.setType(piece.getType());
        }
    }
}

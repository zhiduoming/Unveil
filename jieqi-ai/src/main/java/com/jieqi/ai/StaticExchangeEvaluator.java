package com.jieqi.ai;

import com.jieqi.core.Board;
import com.jieqi.core.ChessPiece;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

/**
 * 静态交换评估 (Static Exchange Evaluation, SEE)。
 *
 * 给定一个吃子着法，模拟在目标格上的连续吃子序列：
 * 双方依次用最便宜的攻击子互吃，每一步都可"停手"。
 * 返回当前走子方在这个交换序列中的最终净子力收益。
 *
 * 典型用例：
 *   - SEE > 0  → 赚的吃子，应优先尝试
 *   - SEE == 0 → 等值交换
 *   - SEE <  0 → 亏的吃子（例如 "拿車换兵" SEE ≈ -850），
 *                在静态搜索 (quiescence) 中直接跳过，
 *                在主搜索的着法排序中放到末尾。
 *
 * 算法不精确处理炮的隔山打子在 X-ray 情况下的攻击线变化，
 * 但对绝大多数明显赚 / 亏的交换判断正确，足以纠正
 * "AI 拿车换兵" 这种基础失误。
 */
public final class StaticExchangeEvaluator {

    private StaticExchangeEvaluator() {}

    public static int see(Board originalBoard, Move move) {
        if (move == null || move.isFlipOnly()) return 0;
        int[] dst = ChessPiece.fromCoord(move.getDestination());
        int[] src = ChessPiece.fromCoord(move.getSource());
        ChessPiece target = originalBoard.getPiece(dst[0], dst[1]);
        if (target == null) return 0;             // 非吃子
        ChessPiece attacker = originalBoard.getPiece(src[0], src[1]);
        if (attacker == null) return 0;

        // 在副本上模拟，避免污染外部棋盘
        Board board = new Board(originalBoard);

        int[] gains = new int[32];
        int n = 0;
        gains[n++] = target.getValue();
        int lastAttackerValue = attacker.getValue();

        board.executeMove(move);
        int sideToMove = (attacker.getColor() == ChessPiece.RED)
                ? ChessPiece.BLACK : ChessPiece.RED;

        while (n < gains.length) {
            // 找出 sideToMove 中能攻击 dst 的最便宜的子
            Move cheapestMove = null;
            int cheapestValue = Integer.MAX_VALUE;
            for (Move m : RuleValidator.generateAllMoves(board, sideToMove)) {
                if (m.isFlipOnly()) continue;
                int[] md = ChessPiece.fromCoord(m.getDestination());
                if (md[0] != dst[0] || md[1] != dst[1]) continue;
                int[] ms = ChessPiece.fromCoord(m.getSource());
                ChessPiece p = board.getPiece(ms[0], ms[1]);
                if (p == null) continue;
                int v = p.getValue();
                if (v < cheapestValue) {
                    cheapestValue = v;
                    cheapestMove = m;
                }
            }
            if (cheapestMove == null) break;

            // 当前方吃掉 dst 上的子（其价值 = 上一回合最后落到 dst 的攻击子）
            gains[n] = lastAttackerValue - gains[n - 1];
            n++;
            lastAttackerValue = cheapestValue;
            board.executeMove(cheapestMove);
            sideToMove = (sideToMove == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        }

        // 反向 minimax：每一层当前方可选"停手"(保持本层 gain) 或 "继续"(承担对方的下一层 gain)
        for (int i = n - 1; i > 0; i--) {
            gains[i - 1] = -Math.max(-gains[i - 1], gains[i]);
        }
        return gains[0];
    }
}

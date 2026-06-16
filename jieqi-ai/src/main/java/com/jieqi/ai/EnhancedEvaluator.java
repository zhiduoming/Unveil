package com.jieqi.ai;

import com.jieqi.core.*;
import java.util.*;

public class EnhancedEvaluator {
    // 与 ChessPiece.getBaseValue 对齐：调高车马炮、过河兵、士象
    private static final int[] PIECE_VALUES = {10000, 900, 400, 450, 50, 200, 200};
    private static int[][] ROOK_POS = new int[10][9];
    private static int[][] KNIGHT_POS = new int[10][9];
    private static int[][] CANNON_POS = new int[10][9];
    private static int[][] PAWN_POS = new int[10][9];

    static {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 9; c++) {
                ROOK_POS[r][c] = (3 - Math.abs(c - 4)) * 5 + (5 - Math.abs(r - 4)) * 3;
                int centerDist = Math.abs(r - 4) + Math.abs(c - 4);
                KNIGHT_POS[r][c] = Math.max(0, 8 - centerDist) * 8;
                CANNON_POS[r][c] = (3 - Math.abs(c - 4)) * 3 + (r >= 3 && r <= 6 ? 10 : 0);
                if (r >= 5) PAWN_POS[r][c] = (r == 6 ? 0 : (9 - r) * 5);
                else PAWN_POS[r][c] = 30 + (5 - r) * 5;
                PAWN_POS[r][c] += (3 - Math.abs(c - 4)) * 2;
            }
        }
    }

    public static int evaluate(Board board, int currentColor) {
        // 性能优化：每次 evaluate 只调用 2 次 generateAllMoves（双方各一次），
        // 把走法列表透传给机动性 / 威胁评估，避免叶子节点重复生成（原先 6 次）。
        List<Move> redMoves = RuleValidator.generateAllMoves(board, ChessPiece.RED);
        List<Move> blackMoves = RuleValidator.generateAllMoves(board, ChessPiece.BLACK);
        int red = evaluateColor(board, ChessPiece.RED, redMoves, blackMoves);
        int black = evaluateColor(board, ChessPiece.BLACK, blackMoves, redMoves);
        int score = red - black;
        return currentColor == ChessPiece.RED ? score : -score;
    }

    private static int evaluateColor(Board board, int color, List<Move> myMoves, List<Move> oppMoves) {
        int score = 0;
        // 子力
        score += evaluateMaterial(board, color);
        // 位置
        score += evaluatePosition(board, color);
        // 机动性（直接用预生成列表大小）
        score += myMoves.size() * 3;
        // 将帅安全
        score += evaluateKingSafety(board, color);
        // 威胁（复用预生成的双方走法）
        score += evaluateThreats(board, color, myMoves, oppMoves);
        // 兵形
        score += evaluatePawnStructure(board, color);
        // 猎杀对方将（残局时主动包围/逼近对方孤将）
        score += evaluateKingHunt(board, color);
        return score;
    }

    private static int evaluateMaterial(Board board, int color) {
        int material = 0;
        double darkValueSum = 0;
        int darkCount = 0;
        for (ChessPiece p : board.getPieces(color)) {
            if (p.isRevealed()) {
                material += PIECE_VALUES[p.getType()];
                boolean crossed = (color == ChessPiece.RED) ? p.getRow() <= 4 : p.getRow() >= 5;
                if (crossed) {
                    // 过河兵在残局威力陡增（可参与杀王），从 +20 提升到 +100
                    if (p.getType() == ChessPiece.PAWN) material += 100;
                    if (p.getType() == ChessPiece.ADVISOR || p.getType() == ChessPiece.BISHOP) material += 30;
                }
            } else {
                darkCount++;
                darkValueSum += ChessPiece.getBaseValue(p.getVirtualType());
            }
        }
        if (darkCount > 0) material += (int)(darkValueSum / darkCount) * darkCount;
        material += darkCount * 5;
        return material;
    }

    private static int evaluatePosition(Board board, int color) {
        int score = 0;
        for (ChessPiece p : board.getPieces(color)) {
            if (!p.isRevealed()) continue;
            int r = p.getRow(), c = p.getCol();
            if (color == ChessPiece.RED) {
                switch (p.getType()) {
                    case ChessPiece.ROOK:   score += ROOK_POS[r][c]; break;
                    case ChessPiece.KNIGHT: score += KNIGHT_POS[r][c]; break;
                    case ChessPiece.CANNON: score += CANNON_POS[r][c]; break;
                    case ChessPiece.PAWN:   score += PAWN_POS[r][c]; break;
                }
            } else {
                int flipR = 9 - r;
                switch (p.getType()) {
                    case ChessPiece.ROOK:   score += ROOK_POS[flipR][c]; break;
                    case ChessPiece.KNIGHT: score += KNIGHT_POS[flipR][c]; break;
                    case ChessPiece.CANNON: score += CANNON_POS[flipR][c]; break;
                    case ChessPiece.PAWN:   score += PAWN_POS[flipR][c]; break;
                }
            }
        }
        return score;
    }

    private static int evaluateKingSafety(Board board, int color) {
        ChessPiece king = null;
        for (ChessPiece p : board.getPieces(color))
            if (p.isRevealed() && p.getType() == ChessPiece.KING) { king = p; break; }
        if (king == null) return -100000;
        int score = 0;
        int kr = king.getRow(), kc = king.getCol();
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = kr + dr, c = kc + dc;
                if (r<0||r>9||c<0||c>8) continue;
                ChessPiece p = board.getPiece(r,c);
                if (p!=null && p.getColor()==color && p.isRevealed() &&
                    (p.getType()==ChessPiece.ADVISOR || p.getType()==ChessPiece.BISHOP))
                    score += 30;
            }
        if (color == ChessPiece.RED) score += (9 - kr) * 10;
        else score += kr * 10;
        if (RuleValidator.isInCheck(board, color)) score -= 150;
        return score;
    }

    private static int evaluateThreats(Board board, int color, List<Move> myMoves, List<Move> oppMoves) {
        int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;
        int score = 0;

        // 一次性扫描对方走法（复用 evaluate() 预生成的列表）：
        //  - 收集对方能吃我方哪些格子，以及"最便宜攻击子"的价值
        Map<String, Integer> cheapestAttackerVal = new HashMap<>();
        for (Move m : oppMoves) {
            if (m.isFlipOnly()) continue;
            ChessPiece target = board.getPiece(m.getDestination());
            if (target == null || target.getColor() != color) continue;
            ChessPiece attacker = board.getPiece(m.getSource());
            if (attacker == null) continue;
            cheapestAttackerVal.merge(m.getDestination(), attacker.getValue(), Math::min);
        }

        // 我方对对方的进攻奖励（软），同时统计我方走法用作粗略防守探测
        Set<String> myAttackSquares = new HashSet<>();
        for (Move m : myMoves) {
            if (m.isFlipOnly()) continue;
            myAttackSquares.add(m.getDestination());
            ChessPiece target = board.getPiece(m.getDestination());
            if (target != null && target.getColor() == oppColor) {
                score += target.getValue() / 8;
            }
        }

        // 对每个被威胁的我方子，按"攻击子贵贱 + 是否有防守候选"分级惩罚
        for (Map.Entry<String, Integer> e : cheapestAttackerVal.entrySet()) {
            String coord = e.getKey();
            ChessPiece p = board.getPiece(coord);
            if (p == null) continue;
            int pv = p.getValue();
            int attackerVal = e.getValue();

            // 防守探测（粗略）：我方是否有走法目标格在该被威胁格的相邻 8 邻域，
            // 用作"附近有友军可能换防"的近似信号。严格的防守判定代价太高（每子一次模拟），
            // 这里用低成本的"周边活跃度"逼近，效果上能让 AI 区分"孤悬子" 与 "扎堆子"。
            int[] pc = ChessPiece.fromCoord(coord);
            boolean nearbyActivity = false;
            for (int dr = -1; dr <= 1 && !nearbyActivity; dr++) {
                for (int dc = -1; dc <= 1 && !nearbyActivity; dc++) {
                    if (dr == 0 && dc == 0) continue;
                    int nr = pc[0] + dr, nc = pc[1] + dc;
                    if (nr < 0 || nr > 9 || nc < 0 || nc > 8) continue;
                    if (myAttackSquares.contains(ChessPiece.toCoord(nr, nc))) {
                        nearbyActivity = true;
                    }
                }
            }

            if (attackerVal < pv) {
                // 对方能用便宜的子吃我方贵子 → 严重挂子（最该避免的情况）
                score -= nearbyActivity ? (pv - attackerVal) / 3 : (pv - attackerVal) / 2;
            } else {
                // 攻击子价值 >= 被吃子价值，对方未必愿意换 → 轻惩
                score -= nearbyActivity ? pv / 16 : pv / 8;
            }
        }
        return score;
    }

    /**
     * 猎杀对方将：当对方除王之外的子力较弱时（残局），主动给出
     *   1) 己方攻击子接近对方王 的奖励
     *   2) 对方王活动空间被压缩 的奖励
     * 这是让 AI "懂得追杀孤王" 的关键评估项。
     */
    private static int evaluateKingHunt(Board board, int color) {
        int oppColor = (color == ChessPiece.RED) ? ChessPiece.BLACK : ChessPiece.RED;

        // 找对方将
        ChessPiece oppKing = null;
        int oppMaterialExclKing = 0;
        for (ChessPiece p : board.getPieces(oppColor)) {
            if (p.isRevealed() && p.getType() == ChessPiece.KING) {
                oppKing = p;
            } else {
                oppMaterialExclKing += p.getValue();
            }
        }
        if (oppKing == null) return 0;
        // 对方非王子力 >= 阈值时不进入"残局猎杀"模式，避免开局乱冲
        if (oppMaterialExclKing >= 1500) return 0;

        // 残局烈度系数：对方非王子力越少，猎杀奖励越大
        double endgameFactor = (1500.0 - oppMaterialExclKing) / 1500.0; // 0~1
        int score = 0;

        // 1) 己方攻击子（车/马/炮/过河兵）逼近对方王
        int kr = oppKing.getRow(), kc = oppKing.getCol();
        for (ChessPiece my : board.getPieces(color)) {
            if (!my.isRevealed()) continue;
            int t = my.getType();
            if (t != ChessPiece.ROOK && t != ChessPiece.KNIGHT
                && t != ChessPiece.CANNON && t != ChessPiece.PAWN) continue;
            // 兵只有过河才算攻击子
            if (t == ChessPiece.PAWN) {
                boolean crossed = (color == ChessPiece.RED) ? my.getRow() <= 4 : my.getRow() >= 5;
                if (!crossed) continue;
            }
            int dist = Math.abs(my.getRow() - kr) + Math.abs(my.getCol() - kc);
            // 距离越近奖励越高：距离 0~17，靠近时给 (17-dist) 的奖励
            int weight = switch (t) {
                case ChessPiece.ROOK   -> 6;
                case ChessPiece.CANNON -> 5;
                case ChessPiece.KNIGHT -> 4;
                case ChessPiece.PAWN   -> 3;
                default -> 0;
            };
            score += Math.max(0, 17 - dist) * weight;
        }

        // 2) 对方王活动空间（4 个九宫格内合法去向）
        int kingMoves = 0;
        for (int[] d : new int[][]{{-1,0},{1,0},{0,-1},{0,1}}) {
            int nr = kr + d[0], nc = kc + d[1];
            if (nr < 0 || nr > 9 || nc < 0 || nc > 8) continue;
            // 九宫格约束
            boolean inPalaceRow = (oppColor == ChessPiece.RED) ? (nr >= 7) : (nr <= 2);
            boolean inPalaceCol = (nc >= 3 && nc <= 5);
            if (!inPalaceRow || !inPalaceCol) continue;
            ChessPiece occ = board.getPiece(nr, nc);
            if (occ != null && occ.getColor() == oppColor) continue;
            kingMoves++;
        }
        // 王没地方走，价值最高（接近被困死）
        score += (4 - kingMoves) * 40;

        // 已经将军：再加一笔（不解将的局面更优）
        if (RuleValidator.isInCheck(board, oppColor)) {
            score += 80;
        }

        // 按残局烈度缩放
        return (int) (score * endgameFactor);
    }

    private static int evaluatePawnStructure(Board board, int color) {
        List<ChessPiece> pawns = new ArrayList<>();
        for (ChessPiece p : board.getPieces(color))
            if (p.isRevealed() && p.getType() == ChessPiece.PAWN) pawns.add(p);
        int score = 0;
        for (int i = 0; i < pawns.size(); i++)
            for (int j = i+1; j < pawns.size(); j++) {
                if (Math.abs(pawns.get(i).getCol() - pawns.get(j).getCol()) == 1 &&
                    Math.abs(pawns.get(i).getRow() - pawns.get(j).getRow()) <= 1)
                    score += 10;
            }
        return score;
    }
}
package com.jieqi.server.ws;

import com.jieqi.core.Board;
import com.jieqi.core.Move;
import com.jieqi.core.RuleValidator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 服务端内置 Bot：只从权威规则引擎生成的合法动作中选择。 */
final class RuleBasedBot {

    Move selectMove(Board board, int color) {
        Board aiBoard = board.createAiPublicView(color);
        List<Move> moves = RuleValidator.generateLegalMoves(aiBoard, color);
        if (moves.isEmpty()) {
            return null;
        }

        List<Move> captures = moves.stream()
                .filter(m -> aiBoard.getPiece(m.getDestination()) != null)
                .sorted(Comparator.comparingInt((Move m) ->
                        aiBoard.getPiece(m.getDestination()).getValue()).reversed())
                .toList();
        if (!captures.isEmpty()) {
            return captures.get(0);
        }
        return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
    }
}

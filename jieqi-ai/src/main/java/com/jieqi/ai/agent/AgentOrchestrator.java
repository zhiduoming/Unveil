package com.jieqi.ai.agent;

import com.jieqi.core.Board;
import com.jieqi.core.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 串行编排：Probability → Endgame（若适用）→ Search。
 */
public class AgentOrchestrator {

    private final List<JieqiSubAgent> agents;

    public AgentOrchestrator() {
        agents = new ArrayList<>();
        agents.add(new ProbabilityAgent());
        agents.add(new EndgameAgent());
        agents.add(new SearchAgent());
        agents.sort(Comparator.comparingInt(JieqiSubAgent::priority));
    }

    public AgentOrchestrator(List<JieqiSubAgent> customAgents) {
        this.agents = new ArrayList<>(customAgents);
        this.agents.sort(Comparator.comparingInt(JieqiSubAgent::priority));
    }

    public Move selectMove(Board board, int color, long timeLimitMs) {
        return selectMove(board, color, timeLimitMs, null);
    }

    public Move selectMove(Board board, int color, long timeLimitMs, Map<String, Integer> repetition) {
        AgentContext ctx = new AgentContext(board, color, timeLimitMs);
        ctx.setRepetitionCount(repetition);
        for (JieqiSubAgent agent : agents) {
            if (!agent.supports(ctx)) {
                continue;
            }
            Move move = agent.contribute(ctx);
            if (move != null) {
                return move;
            }
        }
        return null;
    }
}

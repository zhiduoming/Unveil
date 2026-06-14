package com.jieqi.ai;

import com.jieqi.ai.agent.AgentOrchestrator;
import com.jieqi.core.Board;
import com.jieqi.core.Move;

import java.util.Map;

/**
 * 揭棋 AI 统一入口（作业要求的 Agent 对象），内部为多子 Agent 编排。
 */
public class JieqiAgent {

    private final AgentOrchestrator orchestrator;

    public JieqiAgent() {
        this(new AgentOrchestrator());
    }

    public JieqiAgent(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Move selectMove(Board board, int color) {
        return selectMove(board, color, 55_000L);
    }

    public Move selectMove(Board board, int color, long timeLimitMs) {
        return selectMove(board, color, timeLimitMs, null);
    }

    /**
     * @param repetition 当前重复局面计数（{@link com.jieqi.core.Game#getRepetitionCount()}），
     *                   用于规避长将自判负；可为 null。
     */
    public Move selectMove(Board board, int color, long timeLimitMs, Map<String, Integer> repetition) {
        return orchestrator.selectMove(board, color, timeLimitMs, repetition);
    }
}

package com.jieqi.ai.agent;

import com.jieqi.core.Move;

/**
 * 可选子 Agent 接口（组间 AI 对战可互换实现）。
 */
public interface JieqiSubAgent {

    /** 数值越小越先执行。 */
    int priority();

    boolean supports(AgentContext ctx);

    /** 若本 Agent 负责决策则返回着法，否则返回 {@code null}。 */
    Move contribute(AgentContext ctx);
}

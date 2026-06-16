package com.jieqi.ai.bot;

import com.jieqi.core.Board;
import com.jieqi.core.Move;

import java.util.Map;

/** 统一 AI 选步接口。 */
public interface AiBot {

    AiLevel level();

    Move selectMove(Board authoritativeBoard, int color, long timeLimitMs,
                    Map<String, Integer> repetition);
}

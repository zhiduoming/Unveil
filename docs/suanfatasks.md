# 揭棋 AI 算法优化 — 落地级实施任务书

> 基于张恒基 (Bosprimigenious) 对 jieqi-ai 模块的完整走读分析，交付给执行者直接编码使用。
> 目标：让 Easy/Medium/Hard 三档 AI 差距稳定拉大，修复已知缺陷，加入适合课设答辩的算法增强。

---

## 一、现状速查（已有什么）

### 1.1 三档 AI 入口

**工厂**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/AiBotFactory.java`

| 等级 | 实现类 | 核心策略 |
|---|---|---|
| EASY | `EasyRuleBot` | 启发式排序 + TopK 随机 |
| MEDIUM | `AlphaBetaBot` → `JieqiAgent` → `AgentOrchestrator` | Agent 编排 + Alpha-Beta |
| HARD | `BeliefAlphaBetaBot` | Belief Sampling + 多次 Alpha-Beta |

### 1.2 当前参数配置

**文件**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/AiConfig.java` 第 21-26 行

```java
case EASY  -> new AiConfig(level, Math.min(800L, humanBudgetMs), 5, 0, 0);
case HARD  -> new AiConfig(level, humanBudgetMs, 1, 8, 15);
default    -> new AiConfig(level, humanBudgetMs, 1, 0, 0);  // MEDIUM
```

构造函数参数含义：`(AiLevel level, long timeLimitMs, int topKRandom, int beliefSamples, int maxCandidatesForBelief)`

### 1.3 已有算法清单（答辩可讲）

| 算法 | 位置 | 状态 |
|---|---|---|
| Alpha-Beta 剪枝 | `OptimizedAlphaBeta.alphaBeta()` 第 132-203 行 | ✓ 已实现 |
| 迭代加深 | `OptimizedAlphaBeta.search()` 第 60-111 行 for 循环 | ✓ 已实现 |
| 置换表 (TT) | `TranspositionTable.java` | ✓ 已实现 |
| Zobrist 哈希 | `ZobristHash.java` | ✓ 已实现 |
| 杀手启发 | `KillerHeuristic.java` | ✓ 已实现 |
| 历史启发 | `HistoryHeuristic.java` | ✓ 已实现 |
| PVS (Principal Variation Search) | `OptimizedAlphaBeta.java` 第 78-83、179-184 行 | ✓ 已实现 |
| 静态搜索 (Quiescence) | `OptimizedAlphaBeta.quiescenceSearch()` 第 205-244 行 | ✓ 已实现 |
| SEE 静态交换评估 | `StaticExchangeEvaluator.java` | ✓ 已实现 |
| 长将规避 | `OptimizedAlphaBeta.java` 第 74-89 行 + `isRepeatedCheckRisk()` | ✓ 已实现 |
| Belief Sampling | `BeliefAlphaBetaBot.java` + `BoardSampler.java` | ✓ 已实现 |
| 多 Agent 编排 | `AgentOrchestrator.java` 串联 Probability/Endgame/Search | ✓ 已实现 |
| 暗子期望值 | `ProbabilityAgent` 计算 `ev - oppEv` 存入 `AgentContext` | ⚠ 算但没用 |

---

## 二、已识别的缺陷（必须修）

### 缺陷 1：Hard 时间预算被切太碎

**位置**：`AiConfig.java` 第 24 行

**现状**：`beliefSamples=8, maxCandidatesForBelief=15` → 最多 120 次搜索。
人机预算约 5 秒，AI 自动对弈约 2.5 秒。120 次搜索 ÷ 5 秒 = 每次约 41ms，搜索深度极浅。

**后果**：Hard 可能因为太浅的搜索深度，实际棋力不如 Medium。

**修法**：改参数

```java
case HARD -> new AiConfig(level, humanBudgetMs, 1, 4, 6);
```

即：候选 6 个 × 采样 4 次 = 最多 24 次搜索。5 秒 ÷ 24 ≈ 每次 200ms+，搜索深度足够。

### 缺陷 2：Hard 每个 sample 共享搜索器导致 TT 污染

**位置**：`BeliefAlphaBetaBot.java` 第 20 行

**现状**：

```java
private final OptimizedAlphaBeta search = new OptimizedAlphaBeta();  // 共享实例
```

所有 24 次采样搜索共用一个 `OptimizedAlphaBeta` 实例，置换表和历史启发会在不同 sampled board 之间交叉污染。

**根因**：`ZobristHash` 对未翻开棋子统一用 `state=0`（只区分明子类型），不同采样中暗子填了不同真实 type，但哈希值相同。TT 可能把采样 A 的分数误用给采样 B。

**修法**：删除共享实例的字段，每个 sample 内建局部搜索器

```java
// 删除第 20 行: private final OptimizedAlphaBeta search = new OptimizedAlphaBeta();

// 在 selectMove() 的 sample 循环内（原第 61-62 行位置）改为:
OptimizedAlphaBeta localSearch = new OptimizedAlphaBeta();
OptimizedAlphaBeta.SearchResult result =
        localSearch.search(sample, opp(color), perSample, repetition);
```

### 缺陷 3：Hard 时间分配不公平

**位置**：`BeliefAlphaBetaBot.java` 第 60 行

**现状**：

```java
long perSample = Math.max(30L, (deadline - System.currentTimeMillis()) / Math.max(1, samples - s));
```

只考虑当前 candidate 还剩多少 sample，不考虑后面还有多少 candidate。前几个 candidate 可能吃掉大部分时间，后面 candidate 搜索极浅。

**修法**：全局平均分配

```java
long remaining = deadline - System.currentTimeMillis();
int remainingSlots = samples - s + (candidates.size() - candidateIndex - 1) * samples;
long perSample = Math.max(30L, remaining / Math.max(1, remainingSlots));
```

### 缺陷 4：EndgameAgent 没传 repetition

**位置**：`jieqi-ai/src/main/java/com/jieqi/ai/agent/EndgameAgent.java` 第 27 行

**现状**：

```java
OptimizedAlphaBeta.SearchResult result = search.search(ctx.getBoard(), ctx.getColor(), limit);
```

调用的是 3 参数重载（`OptimizedAlphaBeta.java` 第 31-33 行），内部传 `null` 给 repetition，导致残局时长将规避失效。

**修法**：改成 4 参数版本

```java
OptimizedAlphaBeta.SearchResult result =
        search.search(ctx.getBoard(), ctx.getColor(), limit, ctx.getRepetitionCount());
```

### 缺陷 5：ProbabilityAgent 的 bias 是死值

**位置**：`jieqi-ai/src/main/java/com/jieqi/ai/agent/ProbabilityAgent.java` 第 23-31 行

**现状**：

```java
int ev = board.getExpectedValue(color);
int oppEv = board.getExpectedValue(oppColor);
ctx.setProbabilityBias(ev - oppEv);      // 算了
return null;                              // 但没人读
```

`SearchAgent.contribute()` 和 `EndgameAgent.contribute()` 都直接对原始 board 调 search，从未读取 `ctx.getProbabilityBias()`。`OptimizedAlphaBeta` 和 `EnhancedEvaluator` 也没有接收 bias 的参数。

**修法 A（最小改动）**：直接删掉 `ProbabilityAgent`

- 从 `AgentOrchestrator` 的 agent 列表中移除
- 删除 `ProbabilityAgent.java`
- 风险最低，答辩不提即可

**修法 B（推荐，答辩加分）**：把 bias 接进搜索

在 `OptimizedAlphaBeta.search()` 加一个可选参数：

```java
// 新增 5 参数重载
public SearchResult search(Board board, int color, long timeLimitMs,
                           Map<String, Integer> repetition, int evalBias) {
    // ...
}
```

在 `quiescenceSearch()` 的 `standPat` 计算后加上：

```java
standPat += evalBias;
```

在 `SearchAgent.contribute()` 中读取 bias 并传入：

```java
int bias = ctx.getProbabilityBias();
search.search(ctx.getBoard(), ctx.getColor(), limit, ctx.getRepetitionCount(), bias);
```

**选择**：如果时间充裕选 B，验收前赶工选 A。两个方案都写在下面的改动清单里。

### 缺陷 6：Easy 不够菜

**位置**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/EasyRuleBot.java` 第 27-38 行

**现状**：从启发式排序后的 Top5 中随机选。启发式排序会优先吃高价值子、吃将、翻子、中心移动，所以 Easy 已经会有一定棋力。

**修法**：加入 30% 全随机逃逸

在 `selectMove()` 末尾的 `top.get(ThreadLocalRandom.current().nextInt(top.size()))` 之前加：

```java
// 30% 概率从全部合法走法随机（制造"菜鸟感"）
if (ThreadLocalRandom.current().nextDouble() < 0.30) {
    return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
}
```

同时把 `topKRandom` 从 5 改成 8：

```java
case EASY -> new AiConfig(level, Math.min(500L, humanBudgetMs), 8, 0, 0);
```

---

## 三、新增算法增强（答辩加分项）

### 增强 1：Aspiration Window（最优先，风险最低）

**位置**：`OptimizedAlphaBeta.java` 第 63 行

**现状**：

```java
int alpha = -INF, beta = INF;  // 每层都用全窗口
```

**改法**：利用上一层迭代加深的 `bestScore` 缩小搜索窗口

```java
// 替换第 63 行
int window = 80;  // aspiration window 半宽
int alpha, beta;
if (depth <= 1) {
    alpha = -INF;
    beta = INF;
} else {
    alpha = Math.max(-INF, bestScore - window);
    beta = Math.min(INF, bestScore + window);
}

// ... 搜索循环（不变）...

// 在当前深度搜索结束后（第 97 行 if (!abortSearch) 块内），
// 加 fail-low / fail-high 重搜逻辑：
if (!abortSearch && (currentBest <= alpha || currentBest >= beta)) {
    // fail-low 或 fail-high：全窗口重搜
    alpha = -INF;
    beta = INF;
    // 重新执行当前深度的走法遍历（建议提取成一个 private 方法避免重复代码）
    // 或者简单跳过当前深度，让下一层加深自然覆盖（快但不够精确）
}
```

**为什么最优先**：
- 改动不到 20 行
- 不改变走法正确性
- 大部分局面可减少约 30% 搜索节点
- 答辩可讲："利用迭代加深的单调性，用上一轮分数缩窄搜索窗口"

### 增强 2：Late Move Reductions（可选，有风险）

**位置**：`OptimizedAlphaBeta.alphaBeta()` 第 159-195 行的走法循环内

**现状**：PVS 对所有走法用相同深度 `depth-1`，仅窗口不同。

**改法**：对靠后且安静的走法减深度

在第 163 行 `ChessPiece captured = board.executeMove(move)` 之后，第 166 行 `int score;` 之前加判断：

```java
// 判断是否可以减深度
boolean captureOrCheck = captured != null || RuleValidator.isInCheck(board, oppColor);
int reduction = 0;
if (!captureOrCheck && searched >= 4 && depth >= 3 && !isPV) {
    reduction = 1;  // 减少一层深度
}

// 然后用 depth - 1 - reduction 替代原来的 depth - 1
if (searched == 0) {
    score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, isPV);
} else {
    score = -alphaBeta(board, oppColor, depth - 1 - reduction, -alpha - 1, -alpha, false);
    if (score > alpha && score < beta)
        score = -alphaBeta(board, oppColor, depth - 1, -beta, -alpha, true);
}
```

**约束**（必须遵守）：
- 不吃子时不减
- 将军时不减
- 吃将时不减
- 前 4 个走法不减
- 深度 < 3 不减
- PV 节点不减

### 增强 3：Null-Move Pruning（不建议现在加）

揭棋残局（将帅困毙、zugzwang 局面）不适用标准 null-move。如果加，必须限制在：

- 非将军
- 非残局（子力 > 12）
- `depth >= 4`
- 不在重复局面风险中

**验收前不推荐优先做。** 可作为"后续优化方向"写在答辩 PPT 里。

### 增强 4：Information Set MCTS（只写文档，不实现）

**已有基础**：你的 Hard 用 `BoardSampler` 对隐藏暗子确定性采样（Determinization），本质是 ISMCTS 的简化版。

**答辩可讲**：

> Hard AI 采用 Determinization / Belief Sampling 思想，对隐藏暗子身份进行多次采样确定化，每个确定化局面上执行 Alpha-Beta 搜索，最后选期望收益最高的走法。这本质上是 Information Set Monte Carlo Tree Search 的简化版，比纯 Alpha-Beta 更适合非完全信息博弈。后续可扩展为完整 ISMCTS，用 UCB 公式指导采样和树扩展。

---

## 四、逐文件改动清单

### 4.1 `AiConfig.java`

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/AiConfig.java`

**改动**：第 23-24 行

```java
// 改前
case EASY -> new AiConfig(level, Math.min(800L, humanBudgetMs), 5, 0, 0);
case HARD -> new AiConfig(level, humanBudgetMs, 1, 8, 15);

// 改后
case EASY -> new AiConfig(level, Math.min(500L, humanBudgetMs), 8, 0, 0);
case HARD -> new AiConfig(level, humanBudgetMs, 1, 4, 6);
```

### 4.2 `EasyRuleBot.java`

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/EasyRuleBot.java`

**改动**：在 `selectMove()` 方法中，启发式排序之后、TopK 随机之前，加 30% 全随机分支。

```java
// 排序之后、截取 topK 之前插入：
if (ThreadLocalRandom.current().nextDouble() < 0.30) {
    return moves.get(ThreadLocalRandom.current().nextInt(moves.size()));
}
```

### 4.3 `BeliefAlphaBetaBot.java`

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/bot/BeliefAlphaBetaBot.java`

**改动 A**：删除第 20 行共享搜索器字段

```java
// 删除这行
private final OptimizedAlphaBeta search = new OptimizedAlphaBeta();
```

**改动 B**：sample 循环内（原第 58-63 行位置），每个 sample 新建局部搜索器

```java
Board sample = BoardSampler.fromPublicView(publicView, color, rng);
Board.MoveSnapshot snap = sample.makeMove(candidate);

long remaining = deadline - System.currentTimeMillis();
int remainingSlots = samples - s + (candidates.size() - candidateIndex - 1) * samples;
long perSample = Math.max(30L, remaining / Math.max(1, remainingSlots));

OptimizedAlphaBeta localSearch = new OptimizedAlphaBeta();
OptimizedAlphaBeta.SearchResult result =
        localSearch.search(sample, opp(color), perSample, repetition);
sum -= result.score;

sample.unmakeMove(snap);
```

### 4.4 `EndgameAgent.java`

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/agent/EndgameAgent.java`

**改动**：第 27 行

```java
// 改前
OptimizedAlphaBeta.SearchResult result = search.search(ctx.getBoard(), ctx.getColor(), limit);

// 改后
OptimizedAlphaBeta.SearchResult result =
        search.search(ctx.getBoard(), ctx.getColor(), limit, ctx.getRepetitionCount());
```

### 4.5 `ProbabilityAgent.java` — 二选一

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/agent/ProbabilityAgent.java`

**方案 A（删）**：
1. 删除 `ProbabilityAgent.java`
2. 在 `AgentOrchestrator.java` 中移除对 `ProbabilityAgent` 的注册（约第 15 行的 `agents.add(new ProbabilityAgent())`）

**方案 B（接线）**：
1. `OptimizedAlphaBeta` 新增 5 参数 `search()` 重载，增加 `int evalBias` 参数
2. 在 `quiescenceSearch()` 的 `standPat` 后加上 `standPat += evalBias`
3. 在 `SearchAgent.contribute()` 中读取 `ctx.getProbabilityBias()` 并传入
4. `ProbabilityAgent` 保持不变

### 4.6 `OptimizedAlphaBeta.java` — Aspiration Window

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/OptimizedAlphaBeta.java`

**改动位置**：第 63 行 + 第 97-104 行之间

**改法**：

```java
// 第 63 行：替换
int alpha, beta;
if (depth <= 1) {
    alpha = -INF; beta = INF;
} else {
    alpha = Math.max(-INF, bestScore - 80);
    beta  = Math.min(INF, bestScore + 80);
}

int currentBest = -INF;
Move currentBestMove = null;

// ... 走法遍历循环（不变）...

// 第 97 行 if (!abortSearch) 块内，在 bestMove = currentBestMove 之前加：
if (currentBest <= alpha || currentBest >= beta) {
    // 窗口失败：全窗口重搜当前深度
    alpha = -INF; beta = INF;
    // 重新执行搜索（建议把走法循环提取为 private 方法 depthSearch(int depth, int alpha, int beta)）
    // 简化版做法：直接 continue（跳过当前深度，让下一层自然修正，但回退深度）
    // 完整版做法：提取深度搜索代码，失败时重跑一遍
}
```

> **实现提示**：完整版需要把第 60-111 行的走法遍历部分提取为私有方法，避免代码重复。搜索 `currentBest <= alpha || currentBest >= beta` 失败后用全窗口重新调用该私有方法。

### 4.7 `AgentOrchestrator.java`（仅当选择删 ProbabilityAgent 时）

**路径**：`jieqi-ai/src/main/java/com/jieqi/ai/agent/AgentOrchestrator.java`

**改动**：移除 `agents.add(new ProbabilityAgent())` 这行。

---

## 五、时间预算说明

| 场景 | 预算 | 对应 |
|---|---|---|
| WebSocket 人机对战 | ~5 秒 | 客户端发起 `startAiGame`，服务端等待 AI |
| AI 自动对弈 | ~2.5 秒 | `AIVsAIEnhanced` 或 `startAiBattle` |
| Hard belief sampling | ~5 秒（按改后 24 次搜索 ≈ 每次 200ms+） | 足够搜索到深度 4-6 |
| Medium Alpha-Beta | ~5 秒 | 一般达到深度 8-12 |

**如果 Hard 预算不足（AI battle 只有 2.5s）**：可在 `BeliefAlphaBetaBot` 开头动态降级：

```java
if (timeLimitMs < 3000) {
    samples = 2;
    candidates = Math.min(candidates, 4);
}
```

---

## 六、验收标准

### 6.1 三档差距

- [ ] Easy vs Medium 对弈：Medium 胜率 ≥ 85%（Easy 会走 30% 随机，肯定输多）
- [ ] Medium vs Hard 对弈：Hard 胜率 ≥ 55%（Hard 有 belief sampling，对暗子利用更好）
- [ ] Hard 每一步 ≤ 5 秒（不改动超时机制）

### 6.2 Bug 修复

- [ ] EndgameAgent 调用 search 时传入 repetition（长将规避在残局生效）
- [ ] Hard 的 24 次采样搜索互不污染（每 sample 独立 OptimizedAlphaBeta 实例）
- [ ] Hard 的 24 次搜索时间分配均匀（不会前几个候选吃掉大部分时间）
- [ ] ProbabilityAgent 要么删掉，要么 bias 真正影响搜索

### 6.3 算法增强

- [ ] Aspiration Window 已加（第 2 层开始用窄窗口）
- [ ] Aspiration Window 窗口失败时回退到全窗口重搜
- [ ] LMR 可选：quiet move 且在 searched ≥ 4 时减 1 层

### 6.4 不引入新问题

- [ ] Easy 不会走非法棋
- [ ] Medium 棋力不下降（已有测试 `AiFairnessTest`、`JieqiAgentTest` 继续通过）
- [ ] Hard fallback 到 AlphaBetaBot 的逻辑不变（`BeliefAlphaBetaBot.java` 第 84 行）
- [ ] AI 不超时（不改动现有超时截止机制）

---

## 七、不改的东西（防止过度设计）

| 不做的 | 原因 |
|---|---|
| 完整 MCTS / UCT 树搜索 | 工程量太大，与现有 Alpha-Beta 架构不兼容 |
| Null-Move Pruning | 揭棋残局有 zugzwang 风险 |
| Opening Book 开局库 | 不在当前需求范围内 |
| 神经网络评估函数 | 需要训练数据，超出课设范围 |
| 多线程并行搜索 | 当前单线程够用，加并行复杂度剧增 |
| 改 Board / RuleValidator 的领域逻辑 | AI 层不该动领域层 |

---

## 八、答辩话术建议

**三档算法概述**：

> 入门 AI 使用合法走法生成和启发式 Top-K 随机，保证不走非法棋；标准 AI 使用多 Agent 编排，先做暗子期望评估，再进入 Alpha-Beta 搜索；挑战 AI 引入 belief sampling，对对手暗子身份进行多次确定化采样，在每个确定化局面上执行搜索，最后选期望收益最高的走法。

**搜索增强**：

> 搜索层使用迭代加深、置换表、杀手启发、历史启发、PVS、静态搜索和 SEE 静态交换评估。为进一步提高固定时间预算内的搜索深度，我们加入了 Aspiration Window，利用上一层迭代加深的分数缩窄搜索窗口，通常在多数局面可减少约 30% 无效搜索节点。

**Belief Sampling**：

> 揭棋是非完全信息博弈，标准 Alpha-Beta 无法处理对手暗子的真实身份不确定性。我们采用 Determinization 思想，对对手暗子进行随机采样确定化，构成多个可能真实局面，在每个局面上独立搜索，最后选期望收益最高的走法。这本质上是 Information Set MCTS 的简化实现。

---

## 九、文件变更清单速查

| 操作 | 文件 | 改动量 | 说明 |
|---|---|---|---|
| **改** | `bot/AiConfig.java` | 2 行 | EASY topK 5→8、time 800→500；HARD samples 8→4、candidates 15→6 |
| **改** | `bot/EasyRuleBot.java` | +4 行 | 加 30% 全随机分支 |
| **改** | `bot/BeliefAlphaBetaBot.java` | ~10 行 | 删共享 search、每 sample 独立实例、修正时间分配 |
| **改** | `agent/EndgameAgent.java` | 1 行 | search() 加 repetition 参数 |
| **改/删** | `agent/ProbabilityAgent.java` | — | 方案 A：删文件；方案 B：不改 |
| **改** | `agent/AgentOrchestrator.java` | 1 行 | 方案 A：删 ProbabilityAgent 注册；方案 B：不改 |
| **改** | `OptimizedAlphaBeta.java` | +~25 行 | 加 Aspiration Window |
| **改** | `OptimizedAlphaBeta.java` | +~10 行 | 可选：LMR |

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

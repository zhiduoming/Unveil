#import "template.typ": *
#show: doc => [ #cover(title: "AI 算法优化任务书", subtitle: "AI Tasks — jieqi-ai 模块缺陷修复与算法增强", doc-type: "内部实现指南") #doc ]
#setup-doc(title: "Unveil — AI 算法优化任务书")

= 三档 AI 入口

#table(
  columns: (auto, auto, auto),
  [*等级*], [*实现类*], [*核心策略*],
  [EASY], [EasyRuleBot], [启发式排序 + TopK 随机],
  [MEDIUM], [AlphaBetaBot → JieqiAgent], [Agent 编排 + Alpha-Beta],
  [HARD], [BeliefAlphaBetaBot], [Belief Sampling + 多次 Alpha-Beta],
)

= 已识别缺陷

#table(
  columns: (auto, auto),
  [*缺陷*], [*状态*],
  [Hard 时间预算被切太碎（samples 8→4, candidates 15→6）], [#status-ok],
  [Hard 各 sample 共享搜索器导致 TT 污染], [#status-ok],
  [Hard 时间分配不公平（已改为全局平均分配）], [#status-ok],
  [EndgameAgent 未传 repetition（长将规避失效）], [#status-ok],
  [ProbabilityAgent bias 是死值（已删 ProbabilityAgent）], [#status-ok],
  [Easy 不够菜（加入 30% 全随机）], [#status-ok],
)

= 新增算法增强

#table(
  columns: (auto, auto),
  [*增强*], [*状态*],
  [Aspiration Window（窗口宽度 80）], [#status-ok],
  [LMR 晚着减少], [#status-ok],
  [Null-Move Pruning], [不推荐（揭棋残局 zugzwang 风险）],
  [ISMCTS], [只写文档，不实现],
)

= 改动文件清单

#table(
  columns: (auto, auto, auto),
  [*操作*], [*文件*], [*说明*],
  [改], [bot/AiConfig.java], [EASY/HARD 参数调整],
  [改], [bot/EasyRuleBot.java], [30% 全随机分支],
  [改], [bot/BeliefAlphaBetaBot.java], [独立搜索器 + 公平时间分配],
  [改], [agent/EndgameAgent.java], [search() 加 repetition],
  [删], [agent/ProbabilityAgent.java], [bias 是死值，直接删除],
  [改], [agent/AgentOrchestrator.java], [移除 ProbabilityAgent 注册],
  [改], [OptimizedAlphaBeta.java], [Aspiration Window + LMR],
)

= 验收标准

- Easy vs Medium 对弈：Medium 胜率 ≥ 85%
- Medium vs Hard 对弈：Hard 胜率 ≥ 55%
- Hard 每一步 ≤ 5 秒
- AI 搜索前后棋盘一致
- mvn test 全通过

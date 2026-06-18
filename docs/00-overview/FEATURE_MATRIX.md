# 功能完成度矩阵

> **目标读者**：课程验收教师  
> **状态定义**：保守标注，便于一眼判断「做了什么、做到什么程度」

---

## 四档状态说明

| 状态 | 标签 | 含义 |
|---|---|---|
| ✅ | 已实现 | 代码稳定，可演示，有自动化或集成测试覆盖 |
| ⚡ | 已实现-待强化 | 主流程可用，边界测试、参数调优或错误反馈仍待完善 |
| 🔬 | 实验性扩展 | 有雏形，不作为主验收承诺 |
| 📋 | 规划中 | 有设计或占位，代码未交付 |

---

## 1. 规则引擎（15+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| 规则 | 车/俥直线走子 | `RuleValidator` | ✅ | 明暗子均按棋子规则，与 `BoardMakeMoveTest` 覆盖 |
| 规则 | 马/傌日字走子 | `RuleValidator` | ✅ | 初始局面合法走法生成含马步 |
| 规则 | 炮/砲隔子吃子 | `RuleValidator` | ✅ | 明炮走法与炮架判定 |
| 规则 | 兵/卒过河与横走 | `RuleValidator` | ✅ | 过河前后步长差异 |
| 规则 | 将/帅九宫走子 | `RuleValidator` | ✅ | 九宫内四向一步 |
| 规则 | 士/仕斜走（明士可出九宫） | `DarkPieceRuleTest` | ✅ | 暗士限九宫，明士强化规则有单测 |
| 规则 | 象/相田字走（明象可过河） | `DarkPieceRuleTest` | ✅ | 塞象眼约束有单测 |
| 规则 | 暗子按 virtualType 走子 | `RuleValidator` + `ChessPiece.virtualType` | ✅ | 暗子 type=UNKNOWN |
| 规则 | 暗子首次移动翻子 | `Board.executeMove` + `RandomRevealService` | ✅ | 服务器侧随机揭晓 |
| 规则 | 禁止原地翻子 | `RuleValidator` / `DarkPieceRuleTest.flipOnlyMoveIsNotLegalMove` | ✅ | source ≠ destination |
| 规则 | 禁送将（isMoveLegal） | `KingCapturedRuleTest` / `RuleEdgeCaseTest` | ✅ | `Game.processMove` 返回「不能送将」 |
| 规则 | 将帅照面拒绝 | `RuleEdgeCaseTest.kingsFacingSameFileMoveIsIllegal` | ✅ | 同列无子阻隔时非法 |
| 规则 | 被将时必须解将 | `RuleEdgeCaseTest.whenInCheckOnlyEscapeMovesAreLegal` | ✅ | `generateStrictLegalMoves` |
| 规则 | 将死 / 困毙判定 | `EndgameJudge` / `EndgameJudgeTest` | ✅ | CHECKMATE / STALEMATE |
| 规则 | 长将 6 次判负 | `EndgameJudge` / `RuleEdgeCaseTest` | ⚡ | 计数逻辑已实现，复杂局面需人工复核 |
| 规则 | 长捉 6 次判负 | `EndgameJudge.findChaseTarget` | ⚡ | 将/杀/捉分类边界待补全测试 |
| 规则 | 兵卒长捉判和 | `EndgameJudgeTest.pawnLongChaseDrawOnSixthRepetition` | ⚡ | 与长捉非兵情形分开处理 |
| 规则 | 40 步无吃子和棋 | `EndgameJudge`（noCaptureCount≥80） | ✅ | `RuleEdgeCaseTest.noCaptureDrawAtEightyHalfMoves` |
| 规则 | 校验错误原因码 | `RuleValidator` | ⚡ | 多数路径仅返回 boolean，未区分错误类型 |

---

## 2. 网络对弈（10+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| 网络 | WebSocket JSON 主协议 | `WsGameServer` / `JsonMessages` | ✅ | 端口 8887，对齐 INTERFACE.typ v3.0 |
| 网络 | TCP 文本帧附录 B | `GameServer` / `Protocol` | ✅ | 端口 8888 |
| 网络 | 登录与鉴权 | `WsGameServer` / `GameServerLoginIntegrationTest` | ✅ | login / loginResponse |
| 网络 | 自动匹配与房间 | `MatchmakingService` / `WsGameServerIntegrationTest` | ✅ | match → matchSuccess |
| 网络 | 准备与先手选择 | `WsGameServer` | ✅ | ready / first → gameStart |
| 网络 | 走子与双方同步 | `GameServerMoveIntegrationTest` / WS 集成测试 | ✅ | moveResult + board 广播 |
| 网络 | 非法走法拒绝 | `GameServerIllegalMoveIntegrationTest` / WS 测试 | ✅ | valid=false |
| 网络 | 步时 65s 超时 | `Game` / `GameEndgameTest` | ✅ | 60s + 5s 裕量 |
| 网络 | 聊天 | `GameServerChatIntegrationTest` / WS chat 测试 | ✅ | chat 双向 |
| 网络 | 提和 / 拒和 | `WsGameServerIntegrationTest.agreedDrawEndsGameForBothPlayers` | ✅ | drawOffer / drawResponse |
| 网络 | 认输 | `GameServerResignIntegrationTest` / WS resign 测试 | ✅ | gameOver reason=resign |
| 网络 | 再来一局 rematch | `WsGameServerIntegrationTest` | ⚡ | 600s 房间保留，边界清理待观察 |
| 网络 | 观战（本组扩展 watch） | `WsGameServer.handleWatch` / `WsGameClient` | ⚡ | 单房间单观战者，广播同步走子 |
| 网络 | 人机 WS 房间 | `WsGameServer` createAiRoom | ✅ | aiLevel 参数，集成测试覆盖 |

---

## 3. AI 博弈（8+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| AI | Easy 档（规则启发） | `EasyRuleBot` | ✅ | `AiBotFactoryTest.easyBotReturnsLegalMove` |
| AI | Medium 档（Alpha-Beta） | `AlphaBetaBot` / `JieqiAgent` | ✅ | 迭代加深，约 5s 预算 |
| AI | Hard 档（Belief + 搜索） | `BeliefAlphaBetaBot` | ⚡ | 采样次数受时间限制，深度偏浅 |
| AI | Alpha-Beta 剪枝 | `OptimizedAlphaBeta` | ✅ | 核心搜索引擎 |
| AI | 置换表 + Zobrist | `TranspositionTable` / `ZobristHash` | ✅ | 重复局面哈希 |
| AI | 暗子期望值评估 | `EnhancedEvaluator` / `BoardExpectedValueTest` | ✅ | 明子固定分 + 暗子期望 |
| AI | 不透视对手暗子 | `Board.createAiPublicView` / `AiFairnessTest` | ✅ | 对手暗子 type=UNKNOWN |
| AI | Belief Sampling | `BoardSampler` / `BeliefAlphaBetaBot` | ⚡ | Hard 档，采样场景下 TT 可能污染 |
| AI | 超时 Fallback | `AiBotFactory.selectWithFallback` | ✅ | 超时返回合法随机着 |
| AI | 长将规避惩罚 | `OptimizedAlphaBetaRepetitionTest` | ⚡ | repetitionRisk 启发，非完整规则引擎 |
| AI | 多 Agent 编排 | `AgentOrchestrator` | 🔬 | Probability / Endgame / Search 子 Agent |

---

## 4. 棋谱与复盘（6+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| 复盘 | 文字棋谱记录 | `GameRecord` / `MoveNotation` | ✅ | 行式记法，含揭子标注 |
| 复盘 | 棋谱文件落盘 | `GameRecordStore` → `*.jieqi` | ✅ | `GameRecordStoreTest` |
| 复盘 | 内存复盘时间线 | `ReplayTimeline` / `Game.replayTimeline` | ✅ | 对局进行中可用 |
| 复盘 | 复盘帧（棋盘快照） | `ReplayFrame` | ✅ | stepIndex + boardSnapshot + move |
| 复盘 | 协议 replayRequest / replayFrame | `WsGameServer` / `JsonMessageTypes` | ✅ | WS 集成测试 `replayRequestReturnsFramesAfterResign` |
| 复盘 | replay.json 持久化 | `WsGameServer` 终局保存 | ✅ | `records/<roomId>.replay.json` |
| 复盘 | 客户端复盘命令 | `WsGameClient` replay 子命令 | ⚡ | n/p/g/q，终局上帝视角依赖协议字段 |
| 复盘 | 纯棋谱重走还原 | — | 📋 | 故意不做：翻子随机导致非确定 |

---

## 5. 客户端（6+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| 客户端 | 统一启动菜单 | `jieqi-app/Main` | ✅ | 1–9 选项 |
| 客户端 | 棋盘控制台显示 | `ConsoleUI` / `WsGameClient` | ✅ | 10×9，`?` 暗子 |
| 客户端 | 命令解析（match/move/chat） | `WsGameClient` | ✅ | 交互式 REPL |
| 客户端 | WS 与 TCP 双栈 | `WsGameClient` / `GameClient` | ✅ | 主验收走 WS |
| 客户端 | 本地人机（菜单 6） | `Main` + `AiBotFactory` | ✅ | 三档可选 |
| 客户端 | GUI 图形界面 | — | 📋 | 课程范围外，仅控制台 |
| 客户端 | 终局摘要展示 | `GameSummary` / 客户端输出 | ✅ | 胜负原因与统计 |

---

## 6. 工程化（6+ 项）

| 模块 | 功能 | 实现位置 | 状态 | 备注 |
|---|---|---|---|---|
| 工程 | Maven 多模块（5 模块） | 根 `pom.xml` | ✅ | core ← server/client/ai ← app |
| 工程 | Fat JAR 打包 | `jieqi-app` → `unveil-jieqi.jar` | ✅ | `mvn package -pl jieqi-app -am` |
| 工程 | 自检脚本 | `scripts/verify.ps1` | ✅ | compile + test |
| 工程 | 演示脚本 | `scripts/demo.ps1` | ✅ | 三窗口 WS 演示 |
| 工程 | Docker 部署 | `Dockerfile` / `docker-compose.yml` | ⚡ | WS 8887，文档见 DOCKER_DEPLOYMENT |
| 工程 | 行数统计 | `scripts/count-loc.ps1` | ✅ | 63 主代码 / 114 含测试（`count-loc.ps1`） |
| 工程 | JDK 21 统一 | 各模块 `pom.xml` | ✅ | 与 README、CI 一致 |

---

## 汇总统计（按状态）

| 状态 | 条目数（约） | 说明 |
|---|---|---|
| ✅ 已实现 | 48 | 可直接验收演示 |
| ⚡ 已实现-待强化 | 12 | 主流程 OK，边界或反馈待完善 |
| 🔬 实验性 | 1 | Agent 编排扩展 |
| 📋 规划中 | 3 | GUI、棋谱重走等 |

---

*文档版本：v1.0 · 2026-06-18 · 状态标注随代码演进更新，以仓库最新提交为准*

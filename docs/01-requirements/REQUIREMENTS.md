# 需求清单与边界条件

> **文档导航**：[文档总索引](../README.md) · [验收标准对照](./ACCEPTANCE_CRITERIA.md) · [功能完成度矩阵](../00-overview/FEATURE_MATRIX.md) · [接口协议](../INTERFACE.typ)

**项目**：揭棋对弈程序 Unveil  
**版本**：v1.0 · 2026-06-18  
**维护**：张恒基 (Bosprimigenious) 团队

---

## 状态标签说明

| 标签 | 含义 | 验收含义 |
|------|------|----------|
| ✅ 已实现 | 代码稳定，能演示，有单元/集成测试 | 可直接验收 |
| ⚡ 已实现-待强化 | 能跑，但边界测试待补或参数待调优 | 基础功能 OK，备注限制 |
| 🔬 实验性扩展 | 有雏形，不作为主验收承诺 | 加分项，不承诺稳定 |
| 📋 规划中 | 有设计，代码未开始或未完成 | 不予验收 |

---

## 1. 用户角色

| 角色 | 描述 | 核心诉求 |
|------|------|----------|
| **普通玩家** | 通过控制台客户端连接 WebSocket/TCP 服务器，与真人或 AI 对弈 | 规则正确、走子同步、计时公平、非法着法有明确拒绝 |
| **AI 对手** | 以 `ai_bot_*` 账号接入服务器，按三档难度自动走子 | 不透视暗子、每步限时内返回合法走法、难度可感知 |
| **观战者** | 旁观对局（协议扩展，非主验收路径） | 只读棋盘、不干扰对局；当前为实验性扩展 |
| **教师验收者** | 按课程公共接口验收互操作、规则、AI、文档 | 一键构建运行、协议对齐、演示脚本可复现、测试报告可查 |

---

## 2. 功能需求

### 2.1 A. 揭棋对弈程序（网络）

| # | 需求 | 优先级 | 覆盖状态 | 实现位置 | 备注 |
|---|------|--------|----------|----------|------|
| F1 | 两个客户端经网络连接同一服务器真人对弈 | P0 | ✅ 已实现 | `WsGameServer` + `WsGameClient`（WebSocket 8887）；TCP 见附录 B（8888） | 课程主协议为 WebSocket |
| F2 | 服务器与客户端均校验并拒绝非法着法 | P0 | ⚡ 已实现-待强化 | `RuleValidator`（双端均调用） | 当前返回 `boolean`，未区分错误原因码；客户端提示较笼统 |
| F3 | 禁止送将；走子后己方不能处于被将军状态 | P0 | ✅ 已实现 | `isValidMove` + `isMoveLegal` | 非法时提示「不能送将」 |
| F4 | 棋谱：坐标 9-0 行、a-i 列；每步 source + destination；首翻带 type | P0 | ✅ 已实现 | `GameRecord`、`MoveNotation`；协议见 `INTERFACE.typ` §3–§4 | |
| F5 | 服务器随机决定翻开类型；自动记录棋谱 | P0 | ✅ 已实现 | `RandomRevealService`；`GameRecordStore` | 翻子结果由服务器权威决定 |
| F6 | Move 三属性 + 时间戳；以服务器时间为准 | P0 | ✅ 已实现 | `Move.java`；JSON `fromX/fromY` 映射 | 客户端时间戳不被信任 |
| F7 | 计时器判超时（65s = 60s + 5s 裕量） | P0 | ✅ 已实现 | `WsGameServer` / `Game` | 65s 超时稳定 |
| F8 | 客户端 UI 从简；可选多盘对弈 | P1 | ✅ 已实现 | `ConsoleUI`；WebSocket `roomId` + TCP `gameId` | 控制台为主，无 GUI |
| F9 | 复盘：逐步回看对局帧 | P1 | ✅ 已实现 | `ReplayTimeline`、`ReplayFrame`；`replayRequest` 协议 | 内存复盘 + `records/*.replay.json` 落盘 |
| F10 | 终局摘要（胜者、原因、步数、棋谱路径） | P1 | ✅ 已实现 | `GameSummary`；客户端终局展示 | |
| F11 | 聊天、提和、认输、再来一局 | P1 | ✅ 已实现 | `WsGameServer` 消息处理 | |
| F12 | 断线重连保留局面 | P2 | 🔬 实验性扩展 | — | 当前断线以判负为主 |
| F13 | Redis 分布式房间状态 | P2 | 📋 规划中 | — | 单机内存房间已满足验收 |

### 2.2 B. AI 博弈

| # | 需求 | 优先级 | 覆盖状态 | 实现位置 | 备注 |
|---|------|--------|----------|----------|------|
| F14 | 暗子局面评估含数学期望值 | P0 | ✅ 已实现 | `ExpectedValueEvaluator` / `EnhancedEvaluator` | Expectimax 思想 |
| F15 | Alpha-Beta 剪枝搜索 | P0 | ✅ 已实现 | `OptimizedAlphaBeta` + 置换表/杀手启发 | |
| F16 | Agent 对象封装决策 | P0 | ✅ 已实现 | `JieqiAgent`、`AgentOrchestrator` | 多 Agent 编排 |
| F17 | 可对接网络或本地 Board 自检 | P0 | ✅ 已实现 | `AIVsAIEnhanced`、`WsAIGameClient` | |
| F18 | AI 三档难度（Easy / Medium / Hard） | P0 | ⚡ 已实现-待强化 | `EasyRuleBot` / `AlphaBetaBot` / `BeliefAlphaBetaBot` | Hard 的 belief sampling 参数可持续调优 |
| F19 | AI 不透视对手暗子 | P0 | ✅ 已实现 | `board.createAiPublicView(color)` | 有 `AiFairnessTest` 覆盖 |
| F20 | AI 每步限时内返回（含 fallback） | P0 | ✅ 已实现 | `AiConfig.timeLimitMs` | 超时返回合法随机走法 |

---

## 3. 规则需求（揭棋）

| 规则 | 实现 | 优先级 | 状态 | 备注 |
|------|------|--------|------|------|
| 开局：仅将帅明，其余 15 暗子随机 | `Board.initBoard()` | P0 | ✅ 已实现 | |
| 走暗子按位置角色规则（virtualType） | `ChessPiece.getMoveType()` | P0 | ✅ 已实现 | |
| 暗子走完/吃子后翻开；禁止原地翻子 | `executeMove()`；`RuleValidator` 拒绝 `source==destination` | P0 | ✅ 已实现 | |
| 暗士/暗象限九宫/不过河 | `isValidAdvisorMove()` / `isValidBishopMove()` 暗子分支 | P0 | ✅ 已实现 | 有 `DarkPieceRuleTest` |
| 明士/明象可过河（强化规则） | 明子走法无限界 | P0 | ✅ 已实现 | |
| 塞象眼、蹩马腿不变 | `isValidKnightMove()` / `isValidBishopMove()` | P0 | ✅ 已实现 | |
| 胜负：将死、困毙、认输、超时 | `EndgameJudge` + `Game` 状态机 | P0 | ✅ 已实现 | |
| 和棋：40 步无吃子 | `noCaptureCount >= 80` | P0 | ✅ 已实现 | 双方各 40 步 |
| 长将/长捉判负（6 次），兵卒长捉和 | `EndgameJudge` + `RuleEdgeCaseTest` | P0 | ⚡ 已实现-待强化 | 长捉分类（将/杀/捉）需人工复核边界 |
| MoveSnapshot 搜索回滚 | `Board.makeMove` / `unmakeMove` | P0 | ✅ 已实现 | `BoardUndoTest` |
| 将帅照面禁止 | `RuleValidator` | P0 | ✅ 已实现 | |
| 被将必须解将 | `isMoveLegal` | P0 | ✅ 已实现 | |

---

## 4. 非功能需求

| 维度 | 要求 | 验收标准 | 状态 |
|------|------|----------|------|
| **正确性** | 规则校验服务端权威；双端一致拒绝非法着法 | 非法走法 100% 被拒；核心规则有单元测试 | ✅ |
| **响应时间** | 人类走子确认 < 200ms；AI Easy < 500ms，Medium/Hard < 60s（含网络裕量） | 演示无卡顿；AI 无超时崩溃 | ⚡ |
| **可测试性** | 核心领域可脱离网络单测；`mvn test` 全绿 | ≥ 40 个结构化用例（见 `TEST_CASES.md`） | ✅ |
| **可扩展性** | Maven 多模块；协议与领域解耦 | 5 模块独立编译；`INTERFACE.typ` 版本化 | ✅ |
| **可部署性** | 一键构建 + Fat JAR + Docker Compose | `mvn package` + `verify.ps1` 通过 | ✅ |
| **可互操作性** | 对齐课程 WebSocket JSON 公共接口 v3.0 | 端口 8887；消息类型与 `INTERFACE.typ` 一致 | ✅ |
| **可维护性** | 文档分层；需求-设计-测试可追溯 | `docs/` 六类目录 + 本文件四档状态 | ⚡ |
| **安全性** | 不信任客户端时间戳；走子服务端二次校验 | 伪造 timestamp 被忽略 | ✅ |

---

## 5. 需求优先级汇总

### P0 — 必须（验收阻断项）

- 网络双人对弈（WebSocket 8887）
- 服务端规则校验（走法 + 送将 + 翻子）
- 计时超时（65s）
- 棋谱记录与服务器随机翻子
- AI：期望值评估 + Alpha-Beta + 三档 + 不透视
- 面向对象领域模型（Board / Game / Move 等）
- 组间互操作协议文档

### P1 — 重要（验收加分项）

- 复盘时间线与客户端复盘操作
- 终局摘要与棋谱导出
- 聊天 / 提和 / 认输 / 再来一局
- TCP 附录 B 兼容
- Docker 一键部署
- 演示脚本与自检脚本

### P2 — 扩展（不承诺验收）

- 断线重连
- Redis 分布式房间
- Web 旁观 / Bento 看板
- 精美 GUI
- 排行榜 / 战绩统计
- LLM 心理战 CHAT

---

## 6. 边界与不做项

| 不做项 | 原因 | 替代方案 |
|--------|------|----------|
| 精美 GUI | 课程重点在规则/网络/AI，非前端 | `ConsoleUI` + 交互菜单 |
| 客户端伪造时间戳 | 防作弊，服务器权威 | 服务器覆盖 timestamp |
| Web 前端旁观页 | 超出课设范围 | 协议预留 `SPECTATOR` 扩展 |
| 排行榜 / 用户系统 | 非课程要求 | 本地 `userId` + 密码哈希登录 |
| 分布式多节点 | 复杂度高 | 单机 `WsGameServer` |
| AI 透视对手暗子 | 违反揭棋信息差原则 | `createAiPublicView` |
| 棋谱重放代替快照复盘 | 翻子随机性导致重放不一致 | `ReplayTimeline` 棋盘快照 |

---

## 7. 开放问题追踪

详见 [INTERFACE.typ](../03-interface/INTERFACE.typ) 第十章（编译为 `INTERFACE.pdf`），本组共提出 **Q1–Q44**：

| 类别 | 编号 | 要点 | 状态 |
|------|------|------|------|
| 规则与胜负 | Q1–Q8 | 吃暗子信息、不应将、40 回合计数、长将长捉、兵卒长捉、困毙、照面、暗子士象 | 本组方案已写入协议 |
| 翻子与棋谱 | Q9–Q13 | 服务器随机翻子、seed、禁止原地翻子、棋谱 type 字段 | 已实现 |
| 网络与互操作 | Q14–Q20 | 超时 65s、端口、断线重连、多盘匹配、观战、提和流程 | 核心已实现 |
| AI 博弈 | Q21–Q24 | 期望值评估、Agent 接口、AI 接入、随机性说明 | 已实现 |
| 网络底层与并发 | Q25–Q32 | 粘包、NIO、Redis、分布式计时、Docker | 部分规划 |
| 多 Agent / LLM | Q33–Q39 | Agent 编排、CHAT 心理战、Prompt 脱敏 | 搜索 Agent 已实现 |
| 全栈 UI / DevOps | Q40–Q44 | STATS、Web 旁观、docker-compose | 规划中 |

---

## 8. 与加分项的对应关系

题目要求「先提出问题并完善的小组」可获最多 **+8 分**。本组已完成：

- 问题清单结构化（Q1–Q44，均附暂定方案与影响范围）
- 与公共协议、代码模块（[ARCHITECTURE.md](../02-design/ARCHITECTURE.md)）对齐
- 区分「必做互操作」与「扩展建议」（断线重连、观战等）

后续：老师确认后更新 `INTERFACE` 版本号，在实验报告中附「问题—裁定—修订」对照表。

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

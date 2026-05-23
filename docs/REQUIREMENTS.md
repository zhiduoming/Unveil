# 需求清单与边界条件

> 与 `docs/INTERFACE.md` 同步，所有协议层面的决定已落实到接口文档。  
> 本文件追踪需求覆盖度与开放问题。

## 1. 功能需求（二选一或双做）

### A. 揭棋对弈程序（网络）

| # | 需求 | 覆盖状态 |
|---|------|----------|
| F1 | 两个客户端经 TCP 连接同一服务器真人对弈 | ✅ `GameServer` + `ClientHandler` |
| F2 | 服务器与客户端均校验并拒绝非法着法 | ✅ `RuleValidator`（双端均调用） |
| F3 | 不考虑"不应将"自动判负；走不出不自动判胜 | ✅ `INTERFACE.md` §11.1 + 原因码 `KING_CAPTURED` |
| F4 | 棋谱：坐标 9-0 行、a-i 列；每步 source + destination；首翻带 type | ✅ `INTERFACE.md` §6-§8 |
| F5 | 服务器随机决定翻开类型；自动记录棋谱 | ✅ Board 初始化随机排布；`GameRecord` |
| F6 | Move 三属性 + 时间戳；以服务器时间为准 | ✅ `Move.java` + `INTERFACE.md` §7 |
| F7 | 计时器判超时（65s = 60s + 5s 裕量） | ✅ `INTERFACE.md` §7.3 |
| F8 | 客户端 UI 从简；可选多盘对弈 | ✅ `ConsoleUI`，多盘见 `INTERFACE.md` §12 |

### B. AI 博弈

| # | 需求 | 覆盖状态 |
|---|------|----------|
| F9 | 暗子局面评估含数学期望值 | ✅ `ExpectedValueEvaluator` |
| F10 | Alpha-Beta 剪枝搜索 | ✅ `OptimizedAlphaBeta` + 置换表/杀手启发 |
| F11 | Agent 对象封装决策 | ✅ `JieqiAgent` |
| F12 | 可对接网络或本地 Board 自检 | ✅ `AIVsAIEnhanced` |

## 2. 规则需求（揭棋）

| 规则 | 实现 | 状态 |
|------|------|------|
| 开局：仅将帅明，其余 15 暗子随机 | `Board.initBoard()` | ✅ |
| 走暗子按位置角色规则 | `ChessPiece.getMoveType()` → `virtualType` | ✅ |
| 走完/吃子后翻开；可原地翻子（一回合） | `executeMove()` + `isFlipOnly` | ✅ |
| 明士/明象可过河 | `isValidAdvisorMove()` / `isValidBishopMove()` 无限界 | ✅ |
| 塞象眼、蹩马腿不变 | `isValidKnightMove()` / `isValidBishopMove()` | ✅ |
| 胜负：将死、困毙、认输、超时 | `EndgameJudge` + `Game` 状态机 | ✅ |
| 和棋：40 步无吃子 | `noCaptureCount >= 80` | ✅ |
| 长将/长捉判负（6 次），兵卒长捉和 | `EndgameJudge`：将军/捉子分类，≥6 次 REPETITION_LOSS(7) 或兵卒 REPETITION_DRAW(8) | ✅ |

## 3. 非功能需求

- [x] 面向对象：≥5 领域类（`Board`、`ChessPiece`、`Move`、`Game`、`GameRecord`、`Coordinate`、`PieceType`）
- [x] 组间互操作：`docs/INTERFACE.md` v2.0 协议完整定义
- [x] AI 辅助分析、设计、编程、测试（见各文档及 `TEAM.md`）
- [x] 实验报告：成员分工 + 贡献度百分比（见 `TEAM.md`）

## 4. 边界与不做项

- 不实现精美 GUI（`ConsoleUI` 足够）
- 不处理客户端伪造时间戳（服务器忽略并覆盖）
- "不应将"导致的吃帅赢：由玩家自行发现并执行吃将操作，系统不自动判负

## 5. 开放问题追踪（完善题目定义）

详见 [INTERFACE.typ](./INTERFACE.typ) 第十章（PDF 编译为 `INTERFACE.pdf`），本组共提出 **Q1–Q44**：

| 类别 | 编号 | 要点 |
|------|------|------|
| 规则与胜负 | Q1–Q8 | 吃暗子信息、不应将、40 回合计数、长将长捉、兵卒长捉、困毙、照面、暗子士象 |
| 翻子与棋谱 | Q9–Q13 | 服务器随机翻子、seed、原地翻子、棋谱 type 字段 |
| 网络与互操作 | Q14–Q20 | 超时 65s、端口 8888、断线重连、多盘匹配、观战、提和流程 |
| AI 博弈 | Q21–Q24 | 期望值评估、Agent 接口、AI 接入方式、随机性说明 |
| **I 网络底层与并发** | Q25–Q32 | 粘包/半包 FrameDecoder、payload 上限、NIO、Redis 房间/状态、分布式计时、Docker |
| **II 多 Agent / LLM** | Q33–Q39 | Search/Probability/Endgame 编排、CHAT 心理战、Prompt 脱敏、限速与 fallback |
| **III 全栈 UI / DevOps** | Q40–Q44 | STATS/SPECTATOR、Bento 看板、Web 旁观、docker-compose 多服务 |

状态说明：

- **待确认**：需老师裁定后全体统一
- **本组方案**：已写入本协议 v2.0，建议老师认可为公共标准

## 6. 与加分项的对应关系

题目要求「先提出问题并完善的小组」可获最多 **+8 分**。本组已完成：

- 问题清单结构化（非仅罗列，均附暂定方案与影响范围）
- 与公共协议、代码模块（`docs/ARCHITECTURE.md`）对齐
- 区分「必做互操作」与「v2.1 扩展建议」（如断线重连、观战）

后续：老师确认后更新 `INTERFACE` 版本号并在实验报告中附「问题—裁定—修订」对照表。

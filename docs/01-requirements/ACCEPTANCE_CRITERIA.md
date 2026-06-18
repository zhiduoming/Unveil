# 验收标准对照

> **文档导航**：[文档总索引](../README.md) · [需求清单](./REQUIREMENTS.md) · [测试方案](../05-testing/TEST_PLAN.md) · [演示脚本](../07-presentation/DEMO_SCRIPT.md)

**项目**：揭棋对弈程序 Unveil  
**版本**：v1.0 · 2026-06-18  
**用途**：课程验收时逐项勾选，确保可演示、可复现、可举证。

---

## 状态标签

| 标签 | 含义 |
|------|------|
| ✅ | 已通过验收（有测试或演示记录） |
| ⚡ | 基本通过，存在已知限制 |
| 🔬 | 实验性，不作为主验收 |
| 📋 | 未实现 / 规划中 |

---

## 验收对照表

| 编号 | 课程要求 | 本项目对应 | 验收方式 | 状态 |
|------|----------|------------|----------|------|
| **C1** | 网络揭棋双人对弈 | WebSocket 服务器 `WsGameServer`（端口 **8887**）；客户端 `WsGameClient` | 终端 1：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`；终端 2/3：各启动 `client-ws ws://127.0.0.1:8887 player1 123456` 与 `player2 123456`；双方 `login` → `match` → `ready` → `first`，观察 `gameStart` 与棋盘同步 | ✅ |
| **C2** | 服务器校验非法着法 | `RuleValidator` + `Game.processMove`；非法返回 `error` 消息 | 对局中客户端输入 `move a1 a1`（原地翻子）或 `move a0 a9`（越界/非法），观察服务器返回 `error` 且棋盘不变 | ✅ |
| **C3** | 客户端校验非法着法 | `WsGameClient` 本地预校验 + 服务端二次校验 | 客户端发送明显非法走法（如空位走子），观察本地或服务端拒绝提示 | ⚡ |
| **C4** | 禁止送将 | `RuleValidator.isMoveLegal` | 构造己方将被将军局面，尝试走子使己方仍被将，观察拒绝且提示「不能送将」；运行 `RuleEdgeCaseTest` | ✅ |
| **C5** | 七种棋子走法正确 | `RuleValidator.isValidMove` 七种分支 | 运行 `BoardMakeMoveTest`、`DarkPieceRuleTest`；手动演示马走日、炮打隔子等 | ✅ |
| **C6** | 暗子按位置角色走子 | `ChessPiece.virtualType` + `getMoveType()` | 移动未翻开暗子，观察按原位角色（车马炮等）走法；翻子后按真实身份走 | ✅ |
| **C7** | 翻子随机由服务器决定 | `RandomRevealService` | 同一暗子多次在不同对局翻开，观察 `type` 随机；棋谱记录含 `type` 字段 | ✅ |
| **C8** | 禁止原地翻子 | `RuleValidator` 拒绝 `source == destination` | 客户端 `move e6 e6`，观察拒绝 | ✅ |
| **C9** | 棋谱记录 | `GameRecord` + `GameRecordStore` → `records/*.jieqi` | 完成一局后检查 `records/` 目录生成棋谱文件，含坐标与翻子 type | ✅ |
| **C10** | 超时判负（65s） | `Game` 计时 + `WsGameServer` 广播 | 一方故意不走走子超过 65s，观察 `gameOver` 原因含 `TIMEOUT` | ✅ |
| **C11** | 终局判定（将死/困毙/和棋） | `EndgameJudge` | 运行 `EndgameJudgeTest`、`GameEndgameTest`；演示将死或 40 步无吃子和 | ✅ |
| **C12** | 长将/长捉判负 | `EndgameJudge` 连续 6 次 | 运行 `RuleEdgeCaseTest`；人工构造长将序列验证判负 | ⚡ |
| **C13** | AI 博弈（含期望值 + Alpha-Beta） | `JieqiAgent` / `OptimizedAlphaBeta` / `EnhancedEvaluator` | 菜单选择人机对战或 `ai-ws` 自动接入；观察 AI 5s 内走子；运行 `JieqiAgentTest` | ✅ |
| **C14** | AI 三档难度可区分 | `EasyRuleBot` / `AlphaBetaBot` / `BeliefAlphaBetaBot` | 分别选择 Easy/Medium/Hard 对战，观察 Easy 较快较弱、Hard 较强 | ⚡ |
| **C15** | AI 不透视对手暗子 | `board.createAiPublicView(color)` | 运行 `AiFairnessTest`；检查 AI 仅见对手暗子为 UNKNOWN | ✅ |
| **C16** | 面向对象设计（≥5 领域类） | `Board`、`ChessPiece`、`Move`、`Game`、`GameRecord`、`Coordinate` 等 | 查阅 [DOMAIN_MODEL.md](../02-design/DOMAIN_MODEL.md)；类职责清晰、模块解耦 | ✅ |
| **C17** | 组间互操作文档 | `docs/03-interface/INTERFACE.typ` v3.0 + PDF | 提供 `INTERFACE.pdf`；消息字段与课程公共接口对齐 | ✅ |
| **C18** | 棋谱与复盘 | `ReplayTimeline` + `replayRequest` / `replayFrame` | 终局后客户端输入 `replay` 或 `next`/`prev` 逐步回看；检查 `records/*.replay.json` | ✅ |
| **C19** | Maven 多模块工程 | 根 `pom.xml` 下 5 模块 | `mvn clean package -DskipTests` 五模块 BUILD SUCCESS | ✅ |
| **C20** | 一键自检 | `scripts/verify.ps1` | 运行 `powershell -File scripts/verify.ps1`，全部通过 | ✅ |
| **C21** | TCP 附录 B 兼容（可选） | `GameServer` + `GameClient`（端口 8888） | `server 8888` + `client 127.0.0.1 8888` 完成一局 | ✅ |
| **C22** | Docker 部署（可选） | `docker-compose.yml` | `docker compose up` 后客户端连接容器映射端口 8887 | 🔬 |
| **C23** | 实验报告与分工 | `TEAM.md` + `FINAL_REPORT.md` | 报告含成员分工、贡献度、AI 辅助说明 | ⚡ |
| **C24** | 问题完善加分项 | `INTERFACE.typ` Q1–Q44 | 答辩展示问题清单与暂定方案 | ✅ |

---

## 子规则验收明细（C2 扩展）

以下 5+ 条子规则须在 C2/C5 验收中覆盖：

| 子编号 | 子规则 | 验收操作 | 预期结果 | 状态 |
|--------|--------|----------|----------|------|
| C2-1 | 蹩马腿 | 马前方有子，尝试跳马 | 拒绝 | ✅ |
| C2-2 | 塞象眼 | 象眼有子，尝试飞象 | 拒绝 | ✅ |
| C2-3 | 炮架吃子 | 炮无炮架吃子 | 拒绝 | ✅ |
| C2-4 | 将帅照面 | 走子导致将帅同列无阻隔 | 拒绝 | ✅ |
| C2-5 | 暗士不出九宫 | 暗士尝试出九宫斜走 | 拒绝 | ✅ |
| C2-6 | 暗象不过河 | 暗象尝试过河 | 拒绝 | ✅ |
| C2-7 | 明士/明象强化 | 已翻开士象过河走子 | 允许 | ✅ |

---

## 验收环境要求

| 项目 | 要求 |
|------|------|
| JDK | 21（`java -version` 确认） |
| Maven | 3.9+ |
| 操作系统 | Windows / macOS / Linux |
| 网络 | 本机 127.0.0.1，端口 8887 未被占用 |
| 预检命令 | `mvn test` 全绿后再现场演示 |

---

## 验收证据清单

验收时应准备以下材料：

1. `mvn test` 输出截图或 `docs/05-testing/mvn-test-output.txt`
2. 双客户端对弈截图（含走子与非法拒绝）
3. AI 对战截图（含三档之一）
4. 复盘帧切换截图
5. `records/` 下棋谱与 replay 文件
6. `INTERFACE.pdf` 打印或投屏
7. [DEMO_SCRIPT.md](../07-presentation/DEMO_SCRIPT.md) 按时间线演示

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

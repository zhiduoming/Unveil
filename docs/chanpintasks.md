# 揭棋产品闭环 — 落地级实施任务书

> 基于张恒基 (Bosprimigenious) 对全仓库的产品走读分析，交付给执行者直接编码使用。
> 目标：从"功能堆叠型课设"打磨成"验收时稳定、老师能看懂、用户能顺畅玩"的产品闭环。

---

## 一、产品定位（一句话）

**面向课程验收的揭棋对弈系统，重点展示面向对象设计、网络通信、规则校验、AI 博弈、棋谱复盘和工程化自检。**

这句话决定了优先级排序：

```
规则稳定 > AI 可解释 > 复盘可追溯 > 一键运行 > 演示流程清晰 > 花哨 GUI
```

---

## 二、目标产品闭环

```
启动程序
  ↓
选择模式
  ├── WebSocket 真人对战
  ├── 人机对战（三档可选）
  └── AI 自动对弈
  ↓
开始对局 → 走子 / 聊天 / 认输 / 提和 / 加时
  ↓
终局
  ↓
查看摘要（胜者 / 原因 / 步数 / 吃子 / 棋谱路径）
  ↓
复盘（逐步回看）
  ↓
导出棋谱 → 再来一局 / 返回大厅
```

**当前缺口**：终局后的复盘、摘要、导出这三个环节。

---

## 三、P0：稳定性修复（必须做，防止演示翻车）

### P0-1：修复 Board.MoveSnapshot 快照不完整

**位置**：`jieqi-core/src/main/java/com/jieqi/core/Board.java` 第 17-28 行

**现状**：`MoveSnapshot` 只保存两个字段

```java
// 第 17-28 行
public static final class MoveSnapshot {
    private final Move move;
    private final ChessPiece captured;
    // 仅此两个字段
}
```

`undoMove()` 第 324 行用 `noCaptureCount = 0` 兜底（"准确值较复杂"），这在 AI 频繁 make/unmake 时可能导致 40 步无吃子和棋判定错误。

**为什么重要**：AI 搜索每步会做数百次 make/unmake。如果回滚不完整，棋盘状态会逐渐漂移，导致：
- 40 步无吃子计数错误
- 长将长捉重复判定的 key 变化
- AI 搜索污染棋盘，偶发非法棋

**修法**：扩展 `MoveSnapshot` 字段

```java
public static final class MoveSnapshot {
    private final Move move;
    private final ChessPiece captured;
    // 新增：回滚所需的完整状态
    private final int moveCountBefore;
    private final int noCaptureCountBefore;
    private final boolean movedPieceRevealedBefore;
    private final int movedPieceTypeBefore;         // 仅暗子有意义
    private final int movedPieceVirtualTypeBefore;  // 仅暗子有意义
    private final ChessPiece movedPieceBefore;      // 走子前该子完整快照
    // 如果走子前该格为空，movedPieceBefore 为 null（走子是翻子/移动暗子）
}
```

同时修改 `makeMove()` 第 234-236 行，在构造 `MoveSnapshot` 前收集这些状态。修改 `unmakeMove()` 第 238-242 行（以及被调用的 `undoMove()`），用快照中的值精确恢复而非推导。

**注意**：`ChessPiece` 如果缺少拷贝构造函数，需要在 `ChessPiece` 中加一个：

```java
public ChessPiece(ChessPiece other) {
    // 深拷贝所有字段：type, color, virtualType, revealed, row, col
}
```

**验收标准**：

- [ ] 普通移动 undo 后棋盘完全一致（含 moveCount、noCaptureCount）
- [ ] 吃子后 undo，双方棋子列表一致
- [ ] 暗子翻开后 undo，revealed/type/virtualType 全部恢复
- [ ] `noCaptureCount` 精确恢复（不再是 `= 0` 兜底）
- [ ] 连续 make/unmake 100 次后 positionKey 不变
- [ ] AI 搜索后原 board 不变（加测试：搜索前后比较棋盘哈希）

**测试文件建议**：`jieqi-core/src/test/java/com/jieqi/core/BoardUndoTest.java`

### P0-2：统一删除所有"原地翻子"UI 提示

**问题**：`Game.processMove()` 第 45-47 行已禁止原地翻子，但多处 UI 仍在教用户翻子。

**需要改的文件和位置**：

| 文件 | 行号 | 内容 | 操作 |
|---|---|---|---|
| `jieqi-client/.../WsGameClient.java` | 56 | `move <fx> <fy> <tx> <ty> [flip]` | 删除 `[flip]` |
| `jieqi-client/.../WsGameClient.java` | 57 | `flip 示例: move a 6 a 6 flip   原地翻子` | 整行删除 |
| `jieqi-app/.../Main.java` | 125 | `你的走法 (源 目标 / flip coord / quit):` | 改为 `你的走法 (源 目标 / quit):` |
| `jieqi-app/.../Main.java` | 131 | `请输入走法，例如: a3 a4  或  flip e0` | 改为 `请输入走法，例如: a3 a4` |
| `jieqi-app/.../Main.java` | 138 | `if (input.startsWith("flip "))` | 删除该分支 |
| `jieqi-app/.../Main.java` | 253 | `走法 (源 目标 / flip coord / quit):` | 同上 |
| `jieqi-app/.../Main.java` | 262 | `if (input.startsWith("flip "))` | 同上 |
| `jieqi-client/.../WsGameClient.java` | 136-137 | `flip` 解析逻辑 | 删除 `isFlip` 相关，`source == destination` 直接本地拦截 |

**客户端加本地友好拦截**（`WsGameClient.handleMoveCommand()` 中）：

```java
// 在第 120 行附近，解析坐标后、组装 JSON 前：
if ((p[0] + p[1]).equals(p[2] + p[3])) {
    System.out.println("揭棋规则：禁止原地翻子，请输入移动走法（起点≠终点）。");
    return;
}
```

**服务端兜底保留**：`Game.processMove()` 第 45-47 行的校验作为权威兜底，不动。

**验收标准**：

- [ ] README 中不出现"原地翻子"或 flip 示例
- [ ] WsGameClient 命令菜单不出现 flip
- [ ] Main.java 本地模式不出现 flip 提示
- [ ] Main.java 本地模式不接受 flip 命令
- [ ] 客户端 source=dest 时本地拦截，不等服务器报错
- [ ] 服务端仍保留 `move.isFlipOnly()` 兜底校验

### P0-3：补充规则测试矩阵

**位置**：`jieqi-core/src/test/java/com/jieqi/core/`

**现状**：已有部分测试（`BoardMakeMoveTest`、`BoardAiPublicViewTest`），但缺少系统性的规则边界测试。

**需要新增的测试文件**：`jieqi-core/src/test/java/com/jieqi/core/RuleEdgeCaseTest.java`

**测试清单**（每题一个 `@Test` 方法）：

| # | 测试场景 | 预期结果 |
|---|---|---|
| 1 | 将帅照面（红帅黑将同列且无阻隔） | 走子前 `isMoveLegal` 返回 false |
| 2 | 不能送将（走后自己将被吃） | `isMoveLegal` 返回 false |
| 3 | 被将军后必须解将 | 只有解将的走法合法 |
| 4 | 将死判定（对方无合法走法 + 正在被将） | `checkAfterMove` 返回 CHECKMATE |
| 5 | 困毙判定（对方无合法走法 + 未被将） | `checkAfterMove` 返回 STALEMATE |
| 6 | 吃将（直接吃掉对方将/帅） | `isValidMove` 返回 true，走子方获胜 |
| 7 | 40 步无吃子和棋 | `noCaptureCount >= 40` 时判定和棋 |
| 8 | 长将 6 次判负 | 连续将军方在第 6 次被判负 |
| 9 | 长捉 6 次判负 | 连续捉子方在第 6 次被判负 |
| 10 | 兵/卒长捉判和（特殊规则） | 兵卒达到长捉阈值时判和而非判负 |
| 11 | AI 搜索前后棋盘一致 | 搜索前后 Board 的 positionKey 相同 |

**验收标准**：

- [ ] 所有 11 个测试用例通过
- [ ] `mvn test -pl jieqi-core` 通过
- [ ] `scripts/verify.ps1` 通过

---

## 四、P1：产品完整度（最值得做）

### P1-1：复盘功能

> **与 `fupantasks.md` 的关系**：`fupantasks.md` 已经详细描述了复盘的技术实现（ReplayFrame、ReplayTimeline、ReplayRecordStore、WebSocket 协议、客户端命令）。本任务聚焦于**产品层的集成**——终局后自动提示复盘入口、复盘模式下的用户交互流程。

**产品交互流程**：

**终局后自动提示**（在 `WsGameClient` 的 `handleGameOver` 中）：

```
===== 对局结束 =====
胜者：红方
原因：将死
总步数：48
棋谱已保存：records/room_xxx.jieqi
复盘已保存：records/room_xxx.replay.json

可选操作：
replay   查看复盘
rematch  再来一局
quit     退出
```

**复盘模式**（进入 `replay` 命令后）：

```
===== 复盘模式 =====
步数: 12 / 48    当前轮到: 黑方
上一手: 红方 a6 → a5  (吃: 卒)

  (显示第12步的棋盘)

命令: n(下一步)  p(上一步)  0(开局)  end(终局)
      g 12(跳到第12步)  q(退出复盘)
```

**具体实现要求**：

1. **终局提示**：在 `WsGameClient.handleGameOver()` 末尾打印可选操作（replay / rematch / quit）
2. **复盘命令**：在 `startInteractive()` 的 while 循环中解析 `replay` 命令（完整实现见 `fupantasks.md` 第八节）
3. **棋盘打印**：复盘模式下，`handleReplayFrame` 收到服务器帧后调用已有的棋盘打印方法显示该帧
4. **持久化提示**：终局提示中打印 `records/<gameId>.replay.json` 的实际路径

**前置依赖**：

- `fupantasks.md` 中的 `ReplayFrame`、`ReplayTimeline`、`ReplayRecordStore` 已实现
- `fupantasks.md` 中的 WebSocket 协议 `replayRequest` / `replayFrame` 已实现

**验收标准**：

- [ ] 终局后显示可选操作（含 replay）
- [ ] 输入 `replay` 进入复盘模式，显示第 0 帧
- [ ] `n`/`p` 前后翻页，边界有提示
- [ ] `g 12` 跳到指定步
- [ ] `0` 回到开局，`end` 到终局
- [ ] `q` 退出复盘回到终局菜单
- [ ] 复盘时不改变棋盘状态（只读）

### P1-2：AI 三档难度可感知

**位置**：`jieqi-app/src/main/java/com/jieqi/app/Main.java` 的人机对战入口（`playVsAI`）

**现状**：`playVsAI()` 直接启动对局，没有选难度。

**修法**：在人机对战入口前增加难度选择

```
请选择 AI 难度：
1. 入门 Easy   - 启发式随机 + TopK 选子，适合新手练棋
2. 标准 Medium - Alpha-Beta 搜索 + 置换表 + 静态搜索
3. 挑战 Hard   - Belief Sampling 暗子采样 + Alpha-Beta

请选择 (1-3):
```

同时在 WebSocket 人机对战中（`startAiGame` 消息）展示所选等级的说明。

**对局开始时的摘要**：

```
===== 人机对局 =====
你: 红方  vs  AI (挑战 Hard)
算法: Belief Sampling + Alpha-Beta
时间预算: 5000ms
====================
```

**涉及文件**：

- `jieqi-app/.../Main.java`：加难度选择菜单，把 `AiLevel` 传给 `playVsAI()`
- `jieqi-client/.../WsGameClient.java`：在 `startAiGame` 时显示 AI 等级信息
- `jieqi-ai/.../bot/AiBotFactory.java`：确保从 `AiLevel` 到 Bot 的映射正确（已有）

**验收标准**：

- [ ] 本地人机支持三档难度选择
- [ ] WebSocket 人机支持三档难度选择
- [ ] 选择后打印所选难度和算法说明
- [ ] Easy/Medium/Hard 行为有明显差异（Easy 会走随机，Hard 思考更久）

### P1-3：对局摘要

**触发时机**：终局后（在 `handleGameOver` 或对等的本地方法中）

**显示格式**：

```
===== 对局摘要 =====
房间号   room_20260618_abc123
红方     player1 (你)
黑方     AI-hard
胜者     黑方
原因     将死
总步数   48
总用时   12:34
红方被吃 車 馬 炮 兵兵兵
黑方被吃 俥 傌 兵
触发长将  否
触发长捉  否
棋谱文件 records/room_20260618_abc123.jieqi
复盘文件 records/room_20260618_abc123.replay.json
====================
```

**涉及文件**：

- `WsGameClient.handleGameOver()`：WebSocket 模式
- `Main.java` 的 `playVsAI()` 和 `runLocalTest()`：本地模式
- 需要新增一个 `GameSummary` 数据类（或直接在 Game 上加 getter）

**验收标准**：

- [ ] 真人对局结束后显示摘要
- [ ] 人机对局结束后显示摘要
- [ ] AI 自动对弈结束后显示摘要
- [ ] 摘要包含所有上述字段

---

## 五、P2：锦上添花（时间够再做）

### P2-1：观战模式入口

**现状**：服务端已有 observer 支持（AI 自动对弈时），但客户端没有 `watch` 命令。

**加法**：在 `WsGameClient` 中加 `watch <roomId>` 命令，发送：

```json
{ "messageType": "watch", "roomId": "..." }
```

观战者只接收 moveResult、gameOver 等消息，不能走子。

### P2-2：实时统计命令

在客户端交互循环中加 `stats` 命令：

```
stats
```

显示：

```
当前步数: 24 / --
轮到: 红方
红方剩余: 帅 仕仕 相相 俥 傌傌 炮 兵兵
黑方剩余: 将 士士 象 车 马 砲 卒卒卒卒卒
被吃: 红 車馬炮 | 黑 俥砲
无吃子计数: 8
当前将军: 否
AI 搜索深度: 12  节点: 345678
```

数据源来自 `Game` / `Board` / `OptimizedAlphaBeta` 的公开 getter。

### P2-3：走子错误原因细化

**现状**：`RuleValidator` 返回 boolean，客户端只知道"非法走法"。

**加法**：给 `RuleValidator.isValidMove()` 加返回值，区分：

- 非法走法：马被蹩腿
- 非法走法：炮吃子需要炮架
- 非法走法：将帅不能出九宫
- 非法走法：士不能出九宫
- 非法走法：不能送将
- 非法走法：走完后将帅照面
- 非法走法：被将军，必须先解将

这需要把 `isValidMove` 的返回类型从 `boolean` 改成 `RuleValidator.Result`（含 boolean + reason string）。

### P2-4：棋谱查看命令

在客户端加 `record` 或 `moves` 命令，打印当前棋谱文本：

```
1.  a6 a5
2.  b3 b4
3.  ...
```

这个非常简单，就是打印 `game.getRecord().exportText()`。

---

## 六、文档收口

### 6.1 REQUIREMENTS.md 状态分级

**文件**：`docs/REQUIREMENTS.md`

**问题**：当前大量功能标注"已实现"，但没有区分稳定程度。

**修法**：把状态标签从单一的"已实现"改为四种：

| 标签 | 含义 |
|---|---|
| ✅ 已实现 | 代码稳定、能演示、有测试覆盖 |
| ⚡ 已实现-待强化 | 能跑，但边界情况测试待补 |
| 🔬 实验性扩展 | 有雏形，不作为主验收承诺 |
| 📋 规划中 | 有设计文档，不保证演示 |

**具体改动**（逐条过一遍）：

| 功能 | 当前标注 | 建议改为 |
|---|---|---|
| WebSocket 真人对弈 | 已实现 | ✅ 已实现 |
| AI 三档难度 | 已实现 | ⚡ 已实现-待强化（Hard 参数待调优） |
| 长将长捉判定 | 已实现 | ⚡ 已实现-待强化（边界测试补全后改 ✅） |
| 复盘功能 | — | 📋 规划中（或实现后改 ✅） |
| 断线重连 | 已实现 | 🔬 实验性扩展 |
| Redis 房间状态 | — | 📋 规划中 |
| undoMove 快照 | 已实现 | ⚡ 已实现-待强化 |

**验收标准**：

- [ ] 每个功能点有准确的状态标签
- [ ] "已实现"仅用于能演示+有测试的稳定功能

### 6.2 README 更新

**文件**：`README.md`

**改动**：在保持现有结构的基础上，加一段**演示流程**：

```markdown
## 演示流程

1. **启动 WebSocket 服务器**：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`
2. **客户端 1**：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"`
3. **客户端 2**：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player2 123456"`
4. 客户端操作：`match` → `ready` → `first true` / `first false` → 走子
5. 对局结束后：查看摘要 → `replay` 复盘 → `rematch` 再来一局

**演示脚本**：`scripts/demo.ps1` 一键启动服务器 + 两个客户端。
```

同时在 README 的运行方式部分把 Main 菜单对应用户看到的实际菜单项。

### 6.3 新建演示脚本

**文件**：`scripts/demo.ps1`

**内容**：一键打开三个终端窗口（服务器 + 两个客户端），供验收演示使用。

```powershell
# 演示脚本：启动 WebSocket 服务器 + 两个客户端
# 需要三个独立的终端窗口

Write-Host "启动 WebSocket 服务器 (端口 8887)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='server-ws 8887'"

Start-Sleep -Seconds 3

Write-Host "启动客户端 1 (player1)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='client-ws ws://127.0.0.1:8887 player1 123456'"

Write-Host "启动客户端 2 (player2)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='client-ws ws://127.0.0.1:8887 player2 123456'"

Write-Host "三个窗口已启动，可以开始演示。"
```

### 6.4 INTERFACE.typ 标注扩展消息

**文件**：`docs/INTERFACE.typ`

在现有协议定义中标注哪些消息是**课程公共接口**（老师协议），哪些是**本组扩展**。

例如：

| 消息类型 | 归属 |
|---|---|
| `match` / `ready` / `first` / `move` / `moveResult` / `gameStart` / `gameOver` / `chat` | 课程公共接口 |
| `replayRequest` / `replayFrame` | 本组扩展 |
| `rematchRequest` / `rematchResponse` / `pause` / `addTime` | 本组扩展 |

**目的**：老师问"哪些是你们自己加的"时能立刻回答。

---

## 七、产品化开发顺序

**严格按这个顺序执行，不要并行乱改**：

```
第 1 步：稳定性修复 (P0)
├── 修复 MoveSnapshot（加完整字段 + 测试）
├── 删除所有 flip UI 提示
├── 补充规则测试矩阵
└── 跑 scripts/verify.ps1 验证

第 2 步：复盘闭环 (P1-1)
├── ReplayFrame + ReplayTimeline（在 jieqi-core）
├── Game 内记录每步快照
├── WebSocket 协议支持
├── 客户端复盘命令（replay / n / p / g / q）
├── ReplayRecordStore 落盘
└── 终局后自动提示复盘入口

第 3 步：AI 产品化 (P1-2 + suanfatasks.md)
├── AI 三档选择菜单
├── Easy/Medium/Hard 参数调整
├── 修复 ProbabilityAgent / EndgameAgent
├── 加入 Aspiration Window
└── 终局显示 AI 搜索统计

第 4 步：文档收口 (P1-3 + 文档)
├── 对局摘要（终局后自动显示）
├── REQUIREMENTS.md 状态分级
├── README 演示流程
├── INTERFACE.typ 标注扩展消息
└── 新建 demo.ps1

第 5 步：锦上添花 (P2)
├── 观战模式入口
├── stats 实时统计
├── 走子错误原因细化
└── 棋谱查看命令
```

---

## 八、验收总览（用于最终检查）

| 维度 | 验收项 | 对应任务 |
|---|---|---|
| **可玩** | 正常开始→走子→终局→复盘→再来一局全流程 | P1-1 |
| **可玩** | AI 三档可选，选后显示算法说明 | P1-2 |
| **可玩** | 终局后显示完整摘要 | P1-3 |
| **可信** | undoMove 快照完整，AI 搜索不污染棋盘 | P0-1 |
| **可信** | 所有 UI 不出现原地翻子提示 | P0-2 |
| **可信** | 11 项规则边界测试全部通过 | P0-3 |
| **可信** | AI 不超时、不走非法棋 | P0-3 + suanfatasks.md |
| **可展示** | REQUIREMENTS.md 状态分级，不吹不黑 | 文档收口 |
| **可展示** | README 有完整演示流程 | 文档收口 |
| **可展示** | `demo.ps1` 一键三窗口 | 文档收口 |
| **可展示** | INTERFACE.typ 区分老师协议和本组扩展 | 文档收口 |
| **可展示** | `verify.ps1` 一键通过 | P0-3 |

---

## 九、答辩话术（产品层面）

**回答"你们做了什么"**：

> 我们没有只停留在"能走棋"的层面，而是把它完善成一个完整对弈产品。
>
> 用户可以选择 WebSocket 真人对战、人机对战或 AI 自动对弈；系统在服务器端统一校验规则，客户端给友好提示；每局自动记录文字棋谱和完整复盘时间线，可以从开局到终局逐步回看；AI 分为入门、标准和挑战三档，分别对应启发式随机、Alpha-Beta 搜索和暗子信念采样搜索。
>
> 工程上，我们提供了 Maven 多模块架构、Docker 部署、一键自检脚本和一键演示脚本，保证验收前可以完整构建和测试。

**回答"你们怎么保证稳定"**：

> 我们在三个层面保证稳定性：规则层通过 11 个边界用例的系统测试（将帅照面、长将、困毙等）；AI 层通过 MoveSnapshot 完整回滚机制保证搜索不污染真实棋盘；协议层区分课程公共接口和本组扩展消息，保证与老师协议的互操作性。

---

## 十、文件变更总清单

| 优先级 | 操作 | 文件 | 改动量 | 说明 |
|---|---|---|---|---|
| **P0** | 改 | `jieqi-core/.../Board.java` | ~40 行 | 扩展 MoveSnapshot 字段 + 精确 undo |
| **P0** | 新增 | `jieqi-core/.../BoardUndoTest.java` | ~80 行 | MoveSnapshot 完整性测试 |
| **P0** | 改 | `jieqi-client/.../WsGameClient.java` | ~10 行 | 删 flip 提示 + 本地拦截 |
| **P0** | 改 | `jieqi-app/.../Main.java` | ~10 行 | 删 flip 提示 + 本地模式拦截 |
| **P0** | 新增 | `jieqi-core/.../RuleEdgeCaseTest.java` | ~200 行 | 11 个规则边界测试 |
| **P1** | 改 | `jieqi-client/.../WsGameClient.java` | ~80 行 | 终局提示 + 复盘命令解析 |
| **P1** | 改 | `jieqi-app/.../Main.java` | ~40 行 | AI 难度选择菜单 + 对局摘要 |
| **P1** | 改/新 | `jieqi-core/.../Game.java` | ~15 行 | 对局摘要 getter |
| **P2** | 改 | `jieqi-client/.../WsGameClient.java` | ~20 行 | watch 命令 |
| **P2** | 改 | `jieqi-client/.../WsGameClient.java` | ~30 行 | stats + record 命令 |
| **文档** | 改 | `docs/REQUIREMENTS.md` | ~30 处 | 状态分级 |
| **文档** | 改 | `README.md` | +15 行 | 演示流程 |
| **文档** | 改 | `docs/INTERFACE.typ` | ~10 行 | 标注扩展消息 |
| **文档** | 新增 | `scripts/demo.ps1` | ~15 行 | 一键演示脚本 |

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

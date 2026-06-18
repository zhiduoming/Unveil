# 揭棋复盘功能 — 落地级实施任务书

> 基于张恒基 (Bosprimigenious) 对现有仓库的完整走读分析，交付给执行者直接编码使用。
> 本文不讨论"要不要做"，只描述"改哪个文件、写什么、为什么"。

---

## 一、现状摘要（让你的代码落对位置）

### 1.1 棋局核心：`Game.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/core/Game.java`

已有字段：

- `private Board board` — 当前局面（10×9 矩阵，含明/暗状态）
- `private int currentTurn` — 当前轮到谁
- `private GameStatus status` — 局面状态
- `private final GameRecord record = new GameRecord()` — 文字棋谱（只存 "a6-a5" 这类走法文本）
- `private ChessPiece lastCaptured` — 最近被吃的棋子
- `private final List<ChessPiece> capturedPieces` — 全部被吃棋子

**结论**：Game 已经是棋局权威状态，但缺少**逐步棋盘快照**。复盘的内存数据就应该加在 Game 里。

### 1.2 文字棋谱：`GameRecord.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/record/GameRecord.java`

核心实现：

```java
private final List<String> lines = new ArrayList<>();

public void append(Move move) {
    lines.add(MoveNotation.format(move));
}
```

**结论**：GameRecord 只存文本（如 `1. a6-a5`），不存棋盘。复盘需要的是逐步的 `Board` 快照，不是这种文本。

### 1.3 棋谱落盘：`GameRecordStore.java`

**路径**：`jieqi-server/src/main/java/com/jieqi/server/GameRecordStore.java`

对局结束时会写：

```
records/<gameId>.jieqi
```

内容为 header + `game.getRecord().exportText()`。

**结论**：`records/xxx.jieqi` 只供文字棋谱导出。复盘需要新增 `records/xxx.replay.json`。

### 1.4 WebSocket 房间：`WsRoom.java`

**路径**：`jieqi-server/src/main/java/com/jieqi/server/ws/WsRoom.java`

关键事实：

- 每个房间持有一个 `Game game` 实例
- 对局结束后房间**不会立刻销毁**（保留以等待 rematch），有清理超时窗口
- `rematchCleanupLoop()` 在超时后才清理 finished room

**结论**：对局结束后、房间销毁前，客户端可以直接请求复盘。落盘是为了房间销毁后仍能复盘。

### 1.5 现有棋盘 JSON 工具：`BoardJsonMapper`

**路径**：`jieqi-core/src/main/java/com/jieqi/protocol/json/BoardJsonMapper.java`

已有 `toInitialBoard(board)` 方法，能将棋盘转成 JSON 数组。gameStart 消息已经在用它。

**结论**：复盘帧的棋盘 JSON **必须复用这个格式**，不要另造一套。

---

## 二、复盘数据设计

### 2.1 存储分层

```
第一层：内存（运行时）
Game → ReplayTimeline → List<ReplayFrame>
用于房间存在期间的实时复盘。

第二层：文件（持久化）
records/<gameId>.replay.json
用于房间销毁后重新打开复盘。
```

### 2.2 播放模型

```
stepIndex = 0：开局局面（不含走法，只含初始棋盘快照）
stepIndex = 1：第 1 步之后的局面
stepIndex = 2：第 2 步之后的局面
...
stepIndex = n：终局局面（如被将死后的最后一帧）
```

### 2.3 核心原则

1. **复盘数据放在 Game 层**（`jieqi-core`），不放 WsGameServer，不放在客户端
2. **服务器是复盘权威**，客户端只请求某一帧并显示
3. **每帧都拷贝 Board**（`new Board(board)`），不共享引用
4. **棋盘 JSON 复用现有 `BoardJsonMapper` 格式**

---

## 三、需要新增的 3 个文件

### 3.1 `ReplayFrame.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/record/ReplayFrame.java`

**职责**：复盘中的单帧数据。

**字段**：

| 字段 | 类型 | 说明 |
|---|---|---|
| `stepIndex` | `int` | 帧序号（0 = 开局） |
| `move` | `Move` | 从上一帧到这帧的走法（stepIndex=0 时为 null） |
| `boardSnapshot` | `Board` | 该帧的棋盘完整快照（防御性拷贝） |
| `currentTurn` | `int` | 该帧的当前走子方 |
| `status` | `Game.GameStatus` | 该帧的棋局状态（PLAYING / 终局状态） |
| `timestamp` | `long` | 帧记录时间戳 |
| `captured` | `ChessPiece` | 该步被吃的棋子（无吃子时为 null） |

**构造函数的防御性拷贝**：

```java
this.boardSnapshot = new Board(board);           // 不是 board，是拷贝
this.captured = captured == null ? null : new ChessPiece(captured);
```

**务必注意**：不允许任何 setter。所有字段通过构造函数传入，getter 也要返回防御性拷贝（对于 Board 和 ChessPiece）。

### 3.2 `ReplayTimeline.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/record/ReplayTimeline.java`

**职责**：一局棋的时间线，存所有帧。

**方法**：

| 方法 | 说明 |
|---|---|
| `recordInitial(Board, int, GameStatus)` | 记录第 0 帧（开局）。仅当 frames 为空时才记录 |
| `recordAfterMove(Move, Board, int, GameStatus, ChessPiece)` | 记录走子后的一帧 |
| `getFrame(int index)` | 获取第 index 帧（含越界检查） |
| `size()` | 总帧数 |
| `getFrames()` | 返回不可变列表 |
| `isEmpty()` | 是否为空 |

**关键约束**：

- `recordInitial` 只在 frames 为空时生效（防止重复调用）
- `recordAfterMove` 的 `stepIndex` 自动递增，不需要调用者传

### 3.3 `ReplayRecordStore.java`

**路径**：`jieqi-server/src/main/java/com/jieqi/server/ReplayRecordStore.java`

**职责**：把 `Game` 的 `ReplayTimeline` 全部帧序列化为 JSON 落盘。

**落盘路径**：`records/<gameId>.replay.json`

**JSON 结构**：

```json
{
  "gameId": "room_xxx",
  "status": "RED_WIN",
  "reasonCode": "CHECKMATE",
  "frames": [
    {
      "stepIndex": 0,
      "currentTurn": "red",
      "status": "PLAYING",
      "timestamp": 1718700000000,
      "board": [/* BoardJsonMapper 输出 */]
    },
    {
      "stepIndex": 1,
      "move": { "from": "a6", "to": "a5" },
      "currentTurn": "black",
      "status": "PLAYING",
      "timestamp": 1718700001000,
      "captured": { ... },
      "board": [/* ... */]
    }
  ]
}
```

**参照**：可以照着 `GameRecordStore` 的写法，用 `Gson` + `Files.writeString`。

---

## 四、需要修改的 6 个已有文件

### 4.1 `Game.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/core/Game.java`

**改动点**：

**A. 加字段**：

```java
import com.jieqi.record.ReplayTimeline;
private final ReplayTimeline replayTimeline = new ReplayTimeline();
```

**B. 加 getter**：

```java
public ReplayTimeline getReplayTimeline() { return replayTimeline; }
```

**C. 加开局记录方法**：

```java
public void recordReplayInitialIfNeeded() {
    replayTimeline.recordInitial(board, currentTurn, status);
}
```

**D. 在 `processMove()` 里加每步记录**：

位置：`processMove()` 方法的末尾，在 `board.executeMove(move)` 成功执行之后。

当前 `processMove()` 的结尾大致是：

```java
ChessPiece captured = board.executeMove(move);
// ... 终局判定 ...
// ... 切换 currentTurn ...
return null;
```

需要增加两处 `replayTimeline.recordAfterMove(...)`：

- **终局判定分支内**：记录一步（含终局状态）
- **正常走子分支末尾**：记录一步（含 PLAYING 状态）

最终复盘时间线结构：

```
step 0: 开局（recordInitial）
step 1: 第 1 步后（recordAfterMove）← 被将死时 status 为非 PLAYING
step 2: 第 2 步后
...
```

### 4.2 `JsonMessageTypes.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/protocol/json/JsonMessageTypes.java`

加两个常量：

```java
public static final String REPLAY_REQUEST = "replayRequest";   // C → S
public static final String REPLAY_FRAME   = "replayFrame";     // S → C
```

此文件已经有很多扩展消息类型（如 rematch、pause、addTime），加在这里很自然。

### 4.3 `JsonMessages.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/protocol/json/JsonMessages.java`

新增一个工厂方法：

```java
public static JsonObject replayFrame(
    String roomId,
    int stepIndex,
    int totalSteps,
    ReplayFrame frame
)
```

该方法返回的 JSON 对象结构：

```json
{
  "messageType": "replayFrame",
  "roomId": "...",
  "stepIndex": 0,
  "totalSteps": 42,
  "currentTurn": "red",
  "status": "PLAYING",
  "timestamp": 1718700000000,
  "move": { "from": "a6", "to": "a5" },  // 可选
  "captured": { ... },                     // 可选
  "board": [ /* 用 BoardJsonMapper 格式 */ ]
}
```

**棋盘 JSON 使用的 mapper**：

有两种策略：

| 策略 | 用什么 | 暗子信息 |
|---|---|---|
| 最小改动 | `BoardJsonMapper.toInitialBoard(board)` | 与开局相同（virtualType，不泄露真实身份） |
| 更完整 | 新增 `BoardJsonMapper.toReplayBoard(board)` | visible=false 时额外带 realPiece/virtualPiece 字段 |

**建议用更完整版**（下文 4.4 详述），因为复盘/验收/调试都需要看清暗子真实身份。

### 4.4 `BoardJsonMapper.java`

**路径**：`jieqi-core/src/main/java/com/jieqi/protocol/json/BoardJsonMapper.java`

新增一个方法 `toReplayBoard(Board board)`。

和现有 `toInitialBoard()` 的区别：

| 字段 | `toInitialBoard()` | `toReplayBoard()` |
|---|---|---|
| `visible` | ✓ | ✓ |
| `color` | ✓ | ✓ |
| `piece` | virtualType（不泄露真实身份） | virtualType（保持揭棋规则） |
| `realPiece` | ✗ | **新增**：棋子真实类型 |
| `virtualPiece` | ✗ | **新增**：暗子虚拟移动类型 |

示例输出（一个未揭开的暗子）：

```json
{
  "x": "a",
  "y": 9,
  "color": "black",
  "piece": "ROOK",
  "visible": false,
  "realPiece": "HORSE",
  "virtualPiece": "ROOK"
}
```

**为什么加这两个字段**：

- 复盘终局时需要看清每个暗子到底是谁
- 教师验收时可以检查 AI 暗子分配是否正确
- 普通观战时，按揭棋规则只看 visible 和 piece 即可（不破坏视角规则）

### 4.5 `WsGameServer.java`

**路径**：`jieqi-server/src/main/java/com/jieqi/server/ws/WsGameServer.java`

**改动 A：开局时记录第 0 帧**

`tryStartGame()` 方法内，在设置 `PLAYING` 状态之后加一行：

```java
game.recordReplayInitialIfNeeded();
```

`startAiBattleGame()` 方法内同理：

```java
game.recordReplayInitialIfNeeded();
```

`startRematchGame()` 方法内也要加（因为它 new 了新 Game）。

**改动 B：消息分发加 case**

在 `onMessage()` 方法里的 messageType 分发 switch 中加：

```java
case JsonMessageTypes.REPLAY_REQUEST -> handleReplayRequest(ctx, json);
```

**改动 C：新增 handleReplayRequest 方法**

```java
private void handleReplayRequest(WsPlayerContext ctx, JsonObject json) {
    // 1. 查房间
    // 2. 获取 room.game().getReplayTimeline()
    // 3. 读 stepIndex 参数（可选，默认最后一帧）
    // 4. 边界检查
    // 5. 调用 JsonMessages.replayFrame(...) 发送给客户端
}
```

**改动 D：终局时落盘**

加一个字段：

```java
private final ReplayRecordStore replayRecordStore =
    new ReplayRecordStore("records");
```

在 `broadcastGameOver()` 里，`persistRecord(room.game())` 之后加：

```java
persistReplay(room.game());
```

新增 `persistReplay` 方法（照着 `GameRecordStore` 风格写）。

### 4.6 `WsGameClient.java`

**路径**：`jieqi-client/src/main/java/com/jieqi/client/WsGameClient.java`

**改动 A：加字段**

```java
private int replayIndex = 0;
private int replayTotal = 0;
private boolean replayMode = false;
```

**改动 B：`startInteractive()` 菜单里加命令说明**

在现有命令列表中加入：

```
replay              查看最后一步复盘
replay <step>       查看指定步
replay-next / rn    复盘下一步
replay-prev / rp    复盘上一步
```

**改动 C：命令解析**

在 `startInteractive()` 的 while 循环中加 4 个 if 分支：

| 命令 | 逻辑 |
|---|---|
| `replay` | `requestReplay(-1)` |
| `replay <n>` | 解析整数，`requestReplay(n)` |
| `replay-next` / `rn` | 若 `replayIndex < replayTotal-1`，请求 `replayIndex+1`，否则提示"已经是最后一步" |
| `replay-prev` / `rp` | 若 `replayIndex > 0`，请求 `replayIndex-1`，否则提示"已经是开局" |

**改动 D：新增 `requestReplay(int stepIndex)` 方法**

发送：

```json
{
  "messageType": "replayRequest",
  "stepIndex": 3      // 可选，不传则默认最后一步
}
```

**改动 E：`onMessage()` 里加分发**

```java
case JsonMessageTypes.REPLAY_FRAME -> handleReplayFrame(json);
```

**改动 F：`handleReplayFrame()` 方法**

核心逻辑：

1. 从 JSON 中取出 `stepIndex`、`totalSteps`、`board`、`move`、`captured` 等
2. 更新本地 `replayBoard`（新建一个 Board，用 `BoardJsonMapper.fromBoardJson` 重建）
3. 打印该帧信息到控制台：步数、走法、被吃棋子、当前轮到谁
4. 打印整个棋盘（用已有的显示方法）

**关键问题**：现有 `BoardJsonMapper` 没有 `fromBoardJson` 方法（只有 `applyInitialBoard`，只能更新已有棋盘的明子信息）。

**解决方案**：在 `BoardJsonMapper` 里新增一个静态方法：

```java
public static Board fromBoardJson(JsonArray cells)
```

该方法：

1. `new Board()` 得到一个全空棋盘（需要 Board 有无参构造函数或工厂方法，注意别触发随机暗子初始化）
2. 遍历 cells，对每格创建 `ChessPiece`（含 color、type、revealed 等）
3. `board.setPiece(row, col, piece)`

**改动 G（可选增强）**：客户端支持 `ai` 命令发起人机对弈

在现有的交互命令列表中加入：

```
ai easy          发起人机（简单）
ai medium        发起人机（中等）
ai hard          发起人机（困难）
```

发送 `startAiGame` 消息。

---

## 五、各操作的执行入口时机

### 5.1 记录开局帧

| 场景 | 触发位置 | 代码 |
|---|---|---|
| WebSocket 真人对局 | `WsGameServer.tryStartGame()` | `game.recordReplayInitialIfNeeded()` |
| 人机对弈 | `WsGameServer.startAiBattleGame()` | 同上 |
| 再来一局 (rematch) | `WsGameServer.startRematchGame()` | 同上 |

### 5.2 记录每步帧

**唯一入口**：`Game.processMove()`

所有走子（人类、AI、WebSocket/TCP 触发）最终都经过这里，在这里加 `replayTimeline.recordAfterMove(...)` 即可覆盖全场景。

### 5.3 复盘请求

**入口**：客户端发送 `replayRequest`，服务端 `WsGameServer.handleReplayRequest()` 处理。

可以复盘的条件：
- 客户端在当前房间内
- `room.game().getReplayTimeline()` 非空
- `stepIndex` 在合法范围

### 5.4 复盘落盘

**入口**：`WsGameServer.broadcastGameOver()`

对局结束后生成 `records/<gameId>.replay.json`。

---

## 六、验收标准

### 6.1 内存复盘

- [ ] 真人对局：开局后 `replayTimeline.size() >= 1`
- [ ] 每走一步，`replayTimeline.size()` 增 1
- [ ] 客户端 `replay 0` 看到开局棋盘
- [ ] 客户端 `replay 3` 看到第 3 步后的棋盘
- [ ] 客户端 `replay` 看到最后一步
- [ ] `rn` 在最后一步时提示"已经是最后一步"
- [ ] `rp` 在第 0 帧时提示"已经是开局"
- [ ] AI 自动对弈也能复盘

### 6.2 文件落盘

- [ ] 对局结束后 `records/` 目录下出现 `.replay.json` 文件
- [ ] JSON 文件包含所有帧
- [ ] 每帧包含 `stepIndex`、`move`、`board`、`currentTurn`、`status`

### 6.3 棋盘快照正确性

- [ ] 每帧的 board 是独立拷贝（不是共享引用）
- [ ] 暗子信息在复盘帧中包含真实和虚拟类型
- [ ] 被吃棋子在复盘帧中正确显示

### 6.4 协议

- [ ] `replayFrame` 消息结构符合本文定义
- [ ] 复盘不影响正常对局流程（棋盘不受复盘影响）

---

## 七、不建议现在做的（防止过度设计）

| 不做的 | 原因 |
|---|---|
| GUI 进度条拖动 | 客户端当前是命令行交互 |
| 从 replay.json 恢复对局并继续下 | 复杂度高，需求未明确 |
| 多客户端同步复盘模式 | 先做单客户端 |
| 权限控制（谁可以看复盘） | 当前房间内均可看 |

---

## 八、文件变更清单速查

| 操作 | 文件 | 说明 |
|---|---|---|
| **新增** | `jieqi-core/.../record/ReplayFrame.java` | 复盘单帧 |
| **新增** | `jieqi-core/.../record/ReplayTimeline.java` | 复盘时间线 |
| **新增** | `jieqi-server/.../server/ReplayRecordStore.java` | 复盘落盘 |
| **修改** | `jieqi-core/.../core/Game.java` | 加 ReplayTimeline 字段、加记录调用 |
| **修改** | `jieqi-core/.../json/JsonMessageTypes.java` | 加 REPLAY_REQUEST / REPLAY_FRAME |
| **修改** | `jieqi-core/.../json/JsonMessages.java` | 加 replayFrame() 工厂方法 |
| **修改** | `jieqi-core/.../json/BoardJsonMapper.java` | 加 toReplayBoard() / fromBoardJson() |
| **修改** | `jieqi-server/.../ws/WsGameServer.java` | 开局记录、消息处理、终局落盘 |
| **修改** | `jieqi-client/.../client/WsGameClient.java` | 复盘命令、请求/响应处理 |

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

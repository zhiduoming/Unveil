# Cursor 开发任务清单 — 老师协议完整迁移

> **目标**：以老师 2026 公共接口（WebSocket + JSON）为唯一网络面对外接口，完整补齐 WS 测试、AI 集成、DevOps 统一。保留现有领域层（jieqi-core）不改语义。

---

## 前置知识（Cursor 必读）

### 老师规范完整内容

**来源**：`2026大作业公共接口.docx`、`2026大作业——揭棋.docx`

#### 1. 通信层
- WebSocket + JSON，端口 **8887**
- 依赖：`org.java-websocket:Java-WebSocket:1.5.7` + `com.google.code.gson:gson:2.10.1`
- 每个消息必含 `messageType` 字段

#### 2. C→S 消息（客户端→服务器）

| messageType | 说明 | 字段 |
|-------------|------|------|
| `Login` | 登录 | userId, password |
| `register` | 注册 | userId, password, nickname |
| `startMatch` | 开始匹配 | 无额外字段 |
| `cancelMatch` | 取消匹配（可选） | 无额外字段 |
| `requestFirstHand` | 请求先手（可选，10s 窗口） | wannaFirst: true/false |
| `move` | 走子 | fromX, fromY, toX, toY, isFlip: true/false |
| `ping` | 心跳（可选） | timestamp: 长整型毫秒 |
| `Resign` | 认输 | 无额外字段（**注意大写 R**） |
| `Ready` | 准备就绪 | 无额外字段（**注意大写 R**） |

#### 3. S→C 消息（服务器→客户端）

| messageType | 说明 | 字段 |
|-------------|------|------|
| `loginResult` | 登录结果 | success, message, userId |
| `matchSuccess` | 匹配成功 | roomId, opponentId, opponentNickname |
| `roomInfo` | 房间状态更新 | opponentReady: true/false |
| `gameStart` | 游戏开始 | redPlayerId, blackPlayerId, yourColor, firstHand, initialBoard |
| `moveResult` | 走子结果 | success, valid, move, flipResult(可选) |
| `timeout` | 超时判负 | loserId, winnerId, reason: "timeout" |
| `gameOver` | 游戏结束 | winner: "red"/"black", reason, winnerId |
| `pong` | 心跳回复 | timestamp（原样返回） |
| `error` | 错误信息 | code, message |

#### 4. 错误码

| code | 含义 |
|------|------|
| 1001 | 登录失败（账号或密码错误） |
| 1002 | 重复登录 |
| 2001 | 非法走子（规则不符） |
| 2002 | 未轮到本方走子 |
| 2003 | 超时未走子 |
| 3001 | 房间不存在 |
| 3002 | 匹配失败（无对手） |
| 4001 | JSON 格式错误 |

#### 5. 数据结构

**initialBoard**：`[{x: "a"-"i", y: 0-9, piece: "rook"/"knight"/"cannon"/"bishop"/"guard"/"king"/"pawn", visible: false}]`

**棋子类型枚举**（老师原文）：
- 红方中文 → JSON piece 值：车=Rook, 马=Knight, 炮=Cannon, 相=Bishop, 仕=Guard, 帅=King, 兵=Pawn
- 暗子：visible=false，走第一步后 isFlip=true，服务器揭示 flipResult，visible 变为 true

**move 对象**：`{fromX: "a"-"i", fromY: 0-9, toX: "a"-"i", toY: 0-9, isFlip: true/false}`

**坐标系统**：
- 行：9（顶端，黑方）→ 0（底端，红方）
- 列：a（左）→ i（右）
- 内部存储：`row = 9 - displayRow`，`col = coord.charAt(0) - 'a'`

#### 6. 完整流程

```
Login → loginResult
  → startMatch → matchSuccess (双方)
    → requestFirstHand (可选，10s 窗口)
    → Ready (双方) → roomInfo → gameStart (含 initialBoard)
      → move ↔ moveResult (广播，含 flipResult)
      → ... → gameOver | timeout
```

#### 7. 揭棋规则要点（老师文档）

- 开局：双方将/帅明放九宫原位，其余 15 枚随机暗放
- 走子：暗子按所在位置的虚拟类型规则行走，走完翻为明子
- 翻面选项：可以原地翻暗子（flip only），算一回合
- 士（仕）：可出九宫、可过河，斜走一格
- 象（相）：可过河，塞象眼/蹩马腿规则不变
- 暗子被吃：**吃子方知道类型，被吃方不知道类型**
- 40 回合无吃子（80 半步）→ 和棋
- 长将/长捉：最多 6 次重复 → 判负；兵卒长捉 → 判和
- 超时：每步 1 分钟（含 5s 网络容忍）

---

## 任务 0：INTERFACE.typ 协议文档更新（最高优先级，先于一切代码改动）

> **核心原则：v2.0（28 页）的所有内容一个字不能少，在它的基础上扩展老师 WS+JSON 协议作为正文主协议，TCP 降为附录。文档只能变厚不能变薄。**

**目标文件**：`docs/INTERFACE.typ`

### 0.0 关键警告

**当前 v3.0 的问题**：为了对齐老师 WS+JSON 协议，把 v2.0 的大量深度内容删掉了（Q1-Q44 开放问题、棋盘 Cell 编码规则、MSG_BOARD_STATE 完整示例、各消息详细格式、FrameDecoder 解析伪代码、长将/长捉判例细节、棋子基准价值表等）。**这是错误的**。

**正确做法**：
- v2.0 的**全部内容**（共 28 页、15 章、44 个开放问题、附录）→ **一个字不删**
- 老师 `2026大作业公共接口.docx` 的 WS+JSON 协议 → **新增为正文第 6 章**（原 TCP 协议章改为 WS+JSON）
- 原本的 TCP 协议（v2.0 第 6 章全部 msgType/帧格式/BoardState 编码）→ **完整迁移到附录 B**
- 最终文档应该**超过 30 页**，比 v2.0 更厚，而非更薄

### 0.1 文档重组方案

v2.0 原有 28 页内容按以下方案重组（**保留所有已有内容，在此基础上扩展**）：

```
封面（保留 v2.0 封面设计，版本号改为 v3.1）
目录
===== 以下为正文（主协议：WebSocket + JSON）=====
1. 基础约定
   1.1 术语定义（保留 + 补充老师原文术语）
   1.2 技术约定 → 更新：传输协议主选 WS，默认端口 8887
       附注：「本组同时保留 TCP 文本帧扩展，详见附录 B」

2. 坐标系统
   2.1 坐标定义（保留 — 坐标系统与传输协议无关）
   2.2 坐标转换公式（保留 — fromCoord/toCoord Java 参考实现）

3. 棋子类型编码（保留 v2.0 完整内容）
   3.1 原内部编码表（0-6 将帅车马炮兵士象 + 基准价值）
   3.2 新增：老师 JSON piece 枚举映射表（rook/knight/cannon/bishop/guard/king/pawn）
   3.3 新增：内部编码 ↔ JSON 名 ↔ 中文名三向对照表
   3.4 注意：老师原文 piece 首字母大写（Rook/...），本组统一小写，互操作以联调为准

4. 颜色编码（保留，不变）

5. Move 对象规范（保留 v2.0 完整内容）
   5.1 类定义（原 Java 类）
   5.2 字段规则（首翻必带 type / 非首翻 type 为 null / 时间戳权威 / flipOnly）
   5.3 翻子随机性机制（6 条，一字不改）
   5.4 新增：JSON move 对象格式（fromX/fromY/toX/toY/isFlip ↔ Java Move 映射关系）
   5.5 新增：JSON moveResult 对象（success/valid/move/flipResult）

6. WebSocket + JSON 通信协议（★ 新增 — 老师公共接口，正文主协议）
   6.1 传输层约定
       - WebSocket (RFC 6455)，端口 8887
       - 依赖：Java-WebSocket 1.5.7 + Gson 2.10.1（老师示例指定）
       - 消息格式：单行 JSON 文本，UTF-8 编码
       - 每个消息必含 "messageType" 字段
   6.2 消息格式总览（C→S 9 条 + S→C 10 条的速查总表）
   6.3 客户端 → 服务器消息
       逐条详细说明（共 9 条），每一条包含：
         - messageType 精确值（注意 Login/Resign/Ready 大写，其余小写）
         - 方向、说明、字段表（字段名、类型、必填、含义）
         - ≥1 个 JSON 示例
       消息清单：Login, register, startMatch, cancelMatch, requestFirstHand, Ready, move, ping, Resign
   6.4 服务器 → 客户端消息
       同上格式，逐条详细说明（共 10 条）：
       消息清单：loginResult, matchSuccess, roomInfo, gameStart, moveResult, timeout, gameOver, pong, error
   6.5 公共数据结构
       - initialBoard：[{x, y, piece, visible}]
       - move 对象：{fromX, fromY, toX, toY, isFlip}
       - flipResult：翻出的棋子 JSON 名
   6.6 错误码（teacher error.code）
       1001–4001 完整表（code + 含义 + 对应场景）
   6.7 典型 JSON 示例（老师原文 §4.1–4.5 五组示例原样录入）
       4.1 匹配与先手协商
       4.2 走子与翻子
       4.3 超时处理
       4.4 正常结束
       4.5 心跳
   6.8 本组扩展消息（预留，标注「可选，其他组应兼容忽略」）
   6.9 WS 消息 ↔ 内部 Game/Move 映射说明

7. 典型通信流程（保留 v2.0 时序图 + 新增 WS 流程）
   7.1 WS：匹配与开局时序（Login → startMatch → matchSuccess → Ready → gameStart）
   7.2 WS：正常对弈时序（move ↔ moveResult 广播 + flipResult）
   7.3 WS：先手协商时序（requestFirstHand，10s 窗口）
   7.4 WS：非法着法被拒绝（error 2001 + moveResult valid=false）
   7.5 WS：超时判负（timeout + gameOver）
   7.6 WS：认输（Resign → gameOver reason=resign）
   7.7 WS：断线处理（一方 close → 对方 gameOver reason=disconnect）
   注：原 v2.0 TCP 时序图（7.1–7.5）保留，但移到附录 B

8. 暗子走法与虚拟类型（保留 v2.0 全部内容一字不改）
   8.1 虚拟类型机制（含棋盘虚拟类型位置表）
   8.2 明子强化规则（士离宫过河、象过河、塞象眼/蹩马腿）

9. 胜负与和棋判定（保留 v2.0 全部内容一字不改）
   9.1 胜负条件（将死/困毙/超时/认输/断线/吃将）
   9.2 和棋条件（40回合无吃子/协议和棋/兵卒长捉和）
   9.3 长将/长捉判定（6次重复、哈希判定、兵卒例外）
   9.4 超时判定公式（serverCurrentTime - turnStartTime > 65000）

10. 棋谱记录格式（保留 v2.0 全部内容）
    10.1 每步棋的记法（<步数>. <源>-<目标>[(<type>)] [翻]）
    10.2 棋子类型的棋谱名称（0-6 红黑名称表）
    10.3 服务器棋谱存储（GameRecordStore → records/{gameId}.jieqi）

11. 多盘对弈（保留 v2.0 全部内容）
    11.1 实现要点（Map<String, Game> 线程安全）
    11.2 跨组兼容（WS 下等价处理）

12. 组间联调检查清单（保留 + 更新为 WS 联调项）
    原 v2.0 的 14 条清单全部保留，新增 WS 特有条目：
    - WS 连接建立（ws://host:8887）
    - 收到未知 messageType 不崩溃（静默忽略或 error 4001）
    - initialBoard 能完整解析并重建棋盘
    - 双方 client 收到 moveResult 后本地同步 board
    - flipResult 由服务器生成、客户端不可伪造
    - 超时默认 65s，服务器时间戳判定

13. 待老师确认的开放问题
    ★ 保留 v2.0 全部 Q1–Q44，一条不删
    Q1–Q24（规则、翻子、网络、AI — 必做互操作相关）
    Q25–Q32（I. 网络底层与并发架构）
    Q33–Q39（II. 多智能体协作与大模型赋能）
    Q40–Q44（III. 全栈工程化与极致 UI）
    
    新增 Q45（如适用）：
    「WS 与 TCP 双协议并存期间，组间联调优先级如何确定？」
    本组方案：默认 WS 8887 联调；TCP 8888 作为备用/调试通道

14. 实现状态标注（★ 新增整章）
    按本组实际代码覆盖，逐条标注每项特性的实现状态：
    - ✅ 已实现（代码完整 + 测试通过）
    - ⚠️ 已实现待补测（代码存在但集成测试未覆盖）
    - ❌ 未实现（计划中或待开发）
    
    标注粒度：到每个 messageType、每个规则条款、每个错误码

===== 以下为附录 =====

附录 A：WS JSON messageType 快速参考卡片（新增）
    - C→S 速查表（messageType + 字段 + 示例一行）
    - S→C 速查表（同上）
    - WS 错误码速查

附录 B：TCP 文本帧扩展协议 v2.0（★ 原 v2.0 正文第 6 章完整迁移至此）
    说明文字：「本附录为第一组历史扩展协议（v2.0），供需要 TCP 通信的组参考或调试。
              组间联调默认使用正文 WebSocket + JSON 协议（第 6 章）。」
    
    B.1 消息帧格式（msgType|len|payload\n）
    B.2 消息帧解析规则（含 Java 参考实现）
    B.3 消息类型目录（MSG 1–10 表）
    B.4 各消息详细格式（MSG_LOGIN / MOVE / GAME_STATE / ERROR / QUIT / GAME_OVER / BOARD_STATE / DRAW_REQUEST / RESIGN / CHAT）
        — 逐条保留：字段表、示例、行为约定、Cell 编码规则、完整 BOARD_STATE 开局示例
    B.5 错误码表（100–202）
    B.6 GAME_OVER 原因码（0–9）
    B.7 TCP 典型通信时序图（7.1–7.5 五个时序图，从原第 7 章迁入）

附录 C：版本历史（保留 + 补充）
    v0.1 → v1.0 → v1.1 → v1.2 → v1.3 → v2.0（以上全部保留不变）
    新增：
    v3.0 | 2026-05-29 | 新增加老师 WebSocket + JSON 公共接口为正文主协议（第 6 章）；
                       原 TCP 协议迁移至附录 B；新增 WS 联调清单与实现状态标注
    v3.1 | 2026-05-30 | 全文对齐老师 2026 公共接口规范；补全 C→S/S→C 全部 19 条消息
                       的字段级说明；实现状态标注覆盖全部特性；附录 B 完整保留原 v2.0
                       TCP 协议全部细节
```

### 0.2 内容保留检查清单（必须逐条确认）

Cursor 在更新 INTERFACE.typ 后，必须对照此清单自检，确保 v2.0 的每项内容都在新文档中有对应位置：

| v2.0 内容 | 在新文档中的位置 | 状态 |
|-----------|-----------------|------|
| 术语定义表（暗子/明子/翻子/先手/后手/回合/半步） | §1.1 | 必须保留 |
| 技术约定（TCP UTF-8/LF/超时阈值/时间戳权威） | §1.2（WS 为主，TCP 注见附录 B） | 保留+更新 |
| 坐标定义（行 9-0、列 a-i、棋盘图） | §2.1 | 必须保留 |
| 坐标转换公式（fromCoord/toCoord Java 代码） | §2.2 | 必须保留 |
| 棋子类型编码表（0-6 + 基准价值 + 备注） | §3.1 | 必须保留 |
| 颜色编码 | §4 | 必须保留 |
| Move 类定义（Java 源码） | §5.1 | 必须保留 |
| Move 字段规则（5 条：首翻 type、非首翻 null、时间戳权威、flipOnly 等价、翻子随机性） | §5.2 | 必须保留 |
| 翻子随机性机制（6 条安全说明） | §5.3 | 必须保留 |
| TCP 消息帧格式 msgType\|len\|payload\n | 附录 B §B.1 | 完整迁移 |
| TCP 帧解析规则 + Java 参考实现 | 附录 B §B.2 | 完整迁移 |
| MSG_LOGIN–MSG_CHAT 全部 10 类消息详细格式（含字段表、示例、行为约定） | 附录 B §B.4 | 完整迁移 |
| BOARD_STATE Cell 编码规则（0+type / 1+type / 0? / 1? / .） | 附录 B §B.4.7 | 完整迁移 |
| BOARD_STATE 开局完整示例（含棋盘图 + 帧字节序列） | 附录 B §B.4.7 | 完整迁移 |
| ERROR 错误码表（100–202） | 附录 B §B.5 | 完整迁移 |
| GAME_STATE 子类型（LOGIN_ACK/GAME_START/TURN_CHANGE） | 附录 B §B.4.3 | 完整迁移 |
| GAME_OVER 原因码（0–9） | 附录 B §B.6 | 完整迁移 |
| TCP 5 个时序图（正常对弈/非法着法/超时/提和/认输） | 附录 B §B.7 | 完整迁移 |
| 虚拟类型机制（含棋盘位置表、暗子按虚拟类型行棋说明） | §8.1 | 必须保留 |
| 明子强化规则（士离宫过河、象过河、塞象眼蹩马腿） | §8.2 | 必须保留 |
| 胜负条件（6 条：将死/困毙/超时/认输/断线/不应将吃将） | §9.1 | 必须保留 |
| 和棋条件（3 条：40回合/协议/兵卒长捉） | §9.2 | 必须保留 |
| 长将/长捉判定细节（6次重复、哈希判定、兵卒例外） | §9.3 | 必须保留 |
| 超时判定公式（serverCurrentTime - turnStartTime > 65000） | §9.4 | 必须保留 |
| 棋谱记录格式（source-dest-type-[翻]） | §10.1–10.3 | 必须保留 |
| 多盘对弈实现要点 + 跨组兼容 | §11.1–11.2 | 必须保留 |
| 组间联调检查清单（14 条） | §12（原 TCP 条目保留，新增 WS 条目） | 保留+扩展 |
| Q1–Q44 开放问题（全部） | §13 | 一条不删 |
| 附录 A 消息快速参考卡片（TCP 原版） | 附录 A（改为 WS 速查）+ 附录 B 末尾附 TCP 速查 | 更新 |
| 版本历史 v0.1→v2.0 | 附录 C | 全部保留 + 新增 v3.0/v3.1 |

### 0.3 老师 WS+JSON 协议写入要求

从 `2026大作业公共接口.docx` 原文精确录入以下内容（放在 §6 各子节）：

1. **依赖声明**：Java-WebSocket 1.5.7 + Gson 2.10.1，注明「老师示例指定」
2. **C→S 全部 9 条消息**：逐条列出 messageType + 字段 + 类型 + 必填 + JSON 示例
3. **S→C 全部 10 条消息**：同上
4. **错误码 1001–4001**：完整枚举表
5. **数据结构**：initialBoard 数组格式、move 对象、flipResult
6. **teacher 原文 §4.1–4.5 五个 JSON 示例**：原样录入（匹配与开局、走子与翻子、超时、结束、心跳）
7. **关键大小写**：Login / Resign / Ready 大写开头，其余小写，严格遵循老师原文

### 0.4 排版与编译要求

- 保持现有 Typst 排版风格（蓝色系标题、灰色表格底色、等宽代码块、ASCII 时序图）
- JSON 示例用 `#raw(block: true, lang: "json")` 代码块
- messageType 内联用 `#raw(lang: "json")` 标注
- 表格使用 `#table`（带表头底色），不可以用纯文本凑
- 封面日期更新、版本号 v3.1
- `typst compile docs/INTERFACE.typ docs/INTERFACE.pdf` 零错误零警告
- 预期输出：**≥ 30 页 PDF**（v2.0 有 28 页 + 新增 WS 协议约 6-8 页 + 附录迁移 = 约 34-36 页）

---

## 任务 1：WS 集成测试补齐（最高优先级）

**目标文件**：`jieqi-server/src/test/java/com/jieqi/server/ws/WsGameServerIntegrationTest.java`

**现状**：已有 2 个测试（login→match→ready→gameStart 主干 + ping→pong）。测试框架使用 TestWsClient（继承 WebSocketClient，通过 awaitType 等待）。

**要求**：在**同一个测试类**或新建测试类中补充以下场景（每个场景一个 `@Test`）：

### 1.1 完整对局流程（moveResult + flipResult）
```
场景：两个客户端 match → ready → gameStart → 我方 move → 对方 move
验证点：
  - 红方（先手）发送 move（isFlip=true），收到 moveResult（valid=true, 含 flipResult 字段）
  - 黑方（后手）同样收到该 moveResult 广播
  - 黑方发送 move，双方都收到 moveResult
  - 至少走 3 个回合（红黑白各 1-2 步），每次验证 flipResult 非空（因为第一步必翻子）
```

### 1.2 非法走子（error + moveResult.valid=false）
```
场景：开局后，发送明显违法的 move（如: fromX='e', fromY=9, toX='e', toY=10）
验证点：
  - 收到 error，code=2001（非法走子）
  - 收到 moveResult，valid=false
  - 棋盘状态不变，仍轮到我走
```

### 1.3 未轮到本方走子（error 2002）
```
场景：开局后，黑方在红方未走棋时就发 move
验证点：
  - 黑方收到 error，code=2002
  - 黑方收到 moveResult，valid=false
```

### 1.4 认输流程
```
场景：游戏开始后，一方发送 Resign
验证点：
  - 双方都收到 gameOver
  - gameOver 中 winner="red" 或 "black"，reason="resign"
  - 服务器不再接受 move（房间已删除）
```

### 1.5 超时判负
```
场景：需要能模拟超时。两种方案：
  方案 A（推荐）：给 Game/Move 添加 setter 允许测试注入步时已过期
  方案 B：构造一个只存短超时的 Game 子类
  - 设置 turnStartTime = System.currentTimeMillis() - 70000（已超过 65s）
  - 触发 timeoutLoop（等待最多 2s）
验证点：
  - 双方收到 timeout 消息（loserId, winnerId, reason="timeout"）
  - 双方收到 gameOver，reason="timeout"
```

### 1.6 断线判负
```
场景：一方连接正常关闭（.close()）
验证点：
  - 另一方收到 gameOver，reason="disconnect"
  - 服务器清理了该连接和房间
```

### 1.7 requestFirstHand 先手协商
```
场景：双方 Ready 后，红方发 requestFirstHand wannaFirst=false，黑方发 wannaFirst=true
验证点：
  - 黑方变成红方（先手），收到 gameStart 中 yourColor="red", firstHand=true
  - 原红方收到 gameStart 中 yourColor="black", firstHand=false
```

### 1.8 错误消息类型处理
```
场景：发送未知 messageType（如 "foobar"）
验证点：
  - 收到 error，code=4001（JSON 格式错误）
```

### 1.9 未登录直接匹配
```
场景：不发送 Login，直接发送 startMatch
验证点：
  - 收到 error，code=1001（登录失败）
```

### 实现提示
- 使用现有的 `TestWsClient` 内部类或其扩展
- `connect("name")` 创建客户端，`login()`/`startMatch()`/`ready()` 辅助方法发送 JSON
- `awaitType(messageType, timeoutSeconds)` 等待指定消息
- 步时阈值在 `WsGameServer` 中为 `Protocol.TIMEOUT_THRESHOLD`
- **不要引入新的测试框架依赖**，JUnit 5 + Java-WebSocket 足够

---

## 任务 2：AI 接入 WebSocket（WsAIGameClient）

**目标**：让 AI 引擎能通过 WS 协议接入真人对战，替换当前仅 TCP 8888 的 `EnhancedAIEngine`。

### 2.1 新建 `WsAIGameClient`

**文件**：`jieqi-ai/src/main/java/com/jieqi/ai/WsAIGameClient.java`

继承 `org.java_websocket.client.WebSocketClient`，实现一个自动对弈 AI 客户端。

**行为流程**：
```
1. connectBlocking → onOpen
2. 发送 Login {userId, password}
3. 收到 loginResult.success → 发送 startMatch
4. 收到 matchSuccess → 发送 Ready
5. 收到 gameStart → 解析 initialBoard → 存入 Board 实例
   - 如果是先手/轮到己方 → agent.selectMove → 发送 move
6. 收到 moveResult (valid=true) → board.executeMove(move)
   - 如果是对方的 move：等轮到自己 → agent.selectMove → 发送 move
7. 收到 timeout / gameOver → 打印结果，close()
8. 收到 error → 打印错误码和消息
```

**关键要求**：
- 复用 `com.jieqi.ai.JieqiAgent`（`agent.selectMove(board, color, 55_000)`）
- 复用 `BoardJsonMapper.applyInitialBoard` 加载棋盘
- walk 函数 `handleMoveResult` 中调用 `board.executeMove` 本地同步
- 如果 moveResult 带 `flipResult`，打印日志
- 构造函数：`WsAIGameClient(URI serverUri, String userId, String password)`
- 提供一个 `main` 方法可以直接启动（示例 args: `ws://127.0.0.1:8887 ai_bot_1 pw123`）

### 2.2 修改 `Main.java` 菜单

**文件**：`jieqi-app/src/main/java/com/jieqi/app/Main.java`

**改动**：
1. 新增菜单选项 **9**：「AI 经 WS 自动对弈」（启动一个 WsAIGameClient 自动匹配）
2. 或者新增 CLI 子命令 `ai-ws <url> <userId> <password>`
3. WS 选项（7-8）移到菜单更靠前位置（如 3-4），文案加注「推荐」

### 2.3 本地人机对弈保留

- `EnhancedAIEngine` **不删不改**，它的 `calculateMove(Board gameBoard)` 方法仍被 `Main.playVsAI` 使用
- `AIVsAIEnhanced`（本地双 AI 无网络）**不删不改**
- `PerformanceTest` **不删不改**

---

## 任务 3：DevOps 与入口统一

### 3.1 Dockerfile 切到 WS

**文件**：`Dockerfile`（项目根目录）

改动：
```
- EXPOSE 8888 → EXPOSE 8887
- ENV JIEQI_PORT=8888 → ENV JIEQI_PORT=8887
- ENTRYPOINT ["java", "-jar", "/app/unveil-jieqi.jar", "server", "8888"]
  → ENTRYPOINT ["java", "-jar", "/app/unveil-jieqi.jar", "server-ws", "8887"]
```

### 3.2 docker-compose.yml 切到 WS

**文件**：`docker-compose.yml`（项目根目录）

改动：
```yaml
services:
  jieqi-server:
    build: .
    ports:
      - "${JIEQI_PORT:-8887}:8887"   # 原来 8888:8888
    environment:
      JIEQI_PORT: "8887"             # 原来 8888
```

### 3.3 README.md 更新主路径

**文件**：`README.md`

改动：
- 默认启动命令从 `server 8888` 改为 `server-ws 8887`
- 网络对弈示例使用 WS 路径
- TCP 8888 标注为「附录 B / 兼容调试」

### 3.4 scripts/run-app.ps1 默认参数

**文件**：`scripts/run-app.ps1`

检查并确保默认启动参数为 `server-ws 8887`（如果脚本硬编码了参数）。

---

## 任务 4：协议层补丁与缺陷修复

### 4.1 cancelMatch 房间清理

**文件**：`jieqi-server/src/main/java/com/jieqi/server/ws/WsGameServer.java`

**问题**：`handleCancelMatch` 仅从 `matchQueue` 移除，但如果用户已在 room 中（已配对但未 Ready），room 和对方状态未清理。

**要求**：
- 如果 ctx 已绑定 roomId，取消匹配时：
  - 通知对方「对手取消匹配」（可发送 error code=3002）
  - 将对方也移出 room，重置对方状态
  - 从 rooms map 中移除该 room
  - 重置 ctx.roomId / ctx.ready

### 4.2 README 路径修正

**文件**：`README.md`

如果 README 中有「启动 WebSocket 服务器」的章节，确认命令为 `mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`

---

## 任务 5：客户端体验改进（可选但建议做）

### 5.1 WsGameClient 帮助信息完善

**文件**：`jieqi-client/src/main/java/com/jieqi/client/WsGameClient.java`

当前帮助：`命令: match | ready | move <fx> <fy> <tx> <ty> [flip] | resign | ping | board | quit`

**建议补充**：
- `first <true|false>` 命令说明（请求先手）
- 坐标格式示例：`move a 6 a 5`（从 a6 走到 a5）
- 翻子示例：`move a 6 a 6 flip` 或 `move a 6 a 5 flip`

### 5.2 gameStart 显示更多信息

收到 gameStart 后，打印 `redPlayerId`、`blackPlayerId`、`opponentNickname`，便于识别对手。

---

## 任务 6：PieceJsonMapper 类型名与老师对齐检查

**文件**：`jieqi-core/src/main/java/com/jieqi/protocol/json/PieceJsonMapper.java`

**老师要求**的 JSON piece 值（精确大小写）：
```
rook, knight, cannon, bishop, guard, king, pawn
```

**验证**：`toJsonName(int type)` 返回的值必须与上述完全一致（小写）。如果当前有任何不一致，修正。

同时验证 `fromJsonName(String name)` 能正确解析上述所有值。

---

## 开发顺序建议

```
第 0 步：任务 0（INTERFACE.typ 文档更新 — 文档先行，先于一切代码）
  → typst compile 零错误 + 老师原文对照完整
第 1 步：任务 6（PieceJsonMapper 类型名对齐，最快验证）
第 2 步：任务 1.1–1.9（WS 集成测试补齐）
  → 此时运行 mvn test，所有 WS 测试应全绿
第 3 步：任务 4.1（cancelMatch 修复）
第 4 步：任务 2.1–2.2（AI WS 接入）
  → 启动 WsGameServer → 启动 WsAIGameClient × 2 → 自动对弈至终局
第 5 步：任务 3.1–3.4（DevOps 统一）
  → docker compose up 应启动 WS 8887
第 6 步：任务 0.6（INTERFACE.typ 版本历史更新为 v3.1）
第 7 步：任务 5（客户端体验，可选）
第 8 步：任务 7（生成质量报告）
```

---

## 任务 7：生成自检质量报告（最后一步，必须完成）

**Cursor 完成所有代码改动后，必须自己生成一份质量报告**，写入 `docs/COMPLETION_REPORT.md`。我不看代码，只看这份报告来判断工作是否到位。

### 报告必须包含以下章节：

#### 7.1 任务完成清单

每个任务一条，标注状态（✅ 完成 / ⚠️ 部分完成 / ❌ 未完成），并附一句说明：

```
任务 0 INTERFACE.typ 更新：✅ 已完成，新增 3 个表格、15 条 JSON 示例、实现状态标注
任务 1.1 完整对局流程测试：✅ 通过，3 个回合 flipResult 均非空
任务 1.2 非法走子测试：✅ 通过，error code=2001 + moveResult valid=false
...
```

#### 7.2 完整测试输出

**原文粘贴** `mvn test` 的命令行输出（包括所有测试的 PASS/FAIL、耗时、模块名）。不允许截断或省略，必须完整。格式：

```
$ mvn test -pl jieqi-core,jieqi-server,jieqi-ai
[粘贴完整输出]
```

如有 FAIL，逐条解释失败原因及修复情况。

#### 7.3 新增/修改文件清单

列出所有被改动的文件路径，按模块分组，每条标注改动类型（新建 / 修改 / 删除）：

```
jieqi-core/
  src/main/java/com/jieqi/protocol/json/PieceJsonMapper.java  修改 — 类型名对齐
jieqi-server/
  src/test/.../WsMoveIntegrationTest.java                       新建 — move 合法+非法测试
  ...
```

#### 7.4 协议对照表

以老师规范为基准，逐条对照实现状态。表格：

| 老师规范条目 | 代码实现位置 | 状态 | 备注 |
|-------------|-------------|------|------|
| Login (C→S) | WsGameServer.handleLogin:124 | ✅ | |
| register (C→S) | WsGameServer.handleRegister:142 | ✅ | |
| move (C→S) | WsGameServer.handleMove:200 | ✅ | 含 isFlip, flipResult |
| ... | ... | ... | ... |

必须覆盖老师规范中的**全部** messageType、错误码、数据结构。

#### 7.5 已知问题与遗留项

列出当前仍未解决、需人工判断或等待老师裁定的问题。每项标注严重度（高/中/低）：

```
- [中] AI WS 自动对弈只在本地 localhost 测试过，未与其他组联调
- [低] cancelMatch 修复未覆盖「一方 cancel 时对方已 disconnected」的极端情况
- [待裁定] Q1：暗子被吃方是否应看到棋子类型（当前实现：双方都可见 flipResult）
```

#### 7.6 mvn test 最终统计

```
Tests run: XX, Failures: 0, Errors: 0, Skipped: 0
- jieqi-core: XX tests
- jieqi-server: XX tests (其中 WS 集成测试: XX)
- jieqi-ai: XX tests
```

如果任何模块有失败，必须在此说明原因和修复计划。

---

**重要**：这份报告是你（Cursor）向我证明工作质量的全部依据。我不看代码，只看报告。如果某个任务你说完成了但报告里没有对应的测试输出或文件清单佐证，我会认为该任务未完成。

---

## 不可修改的部分（严格保护）

以下文件/模块**绝对不能改动语义**，只能读、不能改行为：

| 文件/模块 | 原因 |
|-----------|------|
| `jieqi-core/.../Board.java` | 棋盘核心，双协议共用 |
| `jieqi-core/.../ChessPiece.java` | 棋子模型 |
| `jieqi-core/.../Move.java` | 走子数据 |
| `jieqi-core/.../Coordinate.java` | 坐标转换 |
| `jieqi-core/.../RuleValidator.java` | 揭棋全部走法规则 |
| `jieqi-core/.../EndgameJudge.java` | 终局判定（长将/困毙/40回合） |
| `jieqi-core/.../Game.java` | 对局状态机（**例外**：可以为测试添加 setter，不改现有方法签名） |
| `jieqi-core/.../RandomRevealService.java` | 服务器权威翻子 |
| `jieqi-core/.../JsonMessageTypes.java` | messageType 常量（**例外**：新增类型可加，不改现有值） |
| `jieqi-core/.../JsonMessages.java` | 消息工厂方法（**例外**：可新增方法，不改现有方法签名） |
| `jieqi-core/.../JsonErrorCodes.java` | 错误码常量 |
| `jieqi-core/.../BoardJsonMapper.java` | 棋盘↔JSON |
| `jieqi-ai/.../JieqiAgent.java` | AI 统一入口 |
| `jieqi-ai/.../OptimizedAlphaBeta.java` | 搜索算法 |
| `jieqi-ai/.../AgentOrchestrator.java` | Agent 编排 |
| `jieqi-ai/.../EnhancedEvaluator.java` | 局面评估 |
| `jieqi-ai/.../EnhancedAIEngine.java` | TCP AI（保留不动） |
| `jieqi-core/.../Protocol.java` | TCP v2.0（保留不动，附录 B） |
| `jieqi-core/.../FrameDecoder.java` | TCP 帧解码（保留不动） |
| `jieqi-server/.../GameServer.java` | TCP 服务端（保留不动） |
| `jieqi-server/.../ClientHandler.java` | TCP 处理器（保留不动） |
| `jieqi-client/.../GameClient.java` | TCP 客户端（保留不动） |

**对于 `WsGameServer.java` 和 `WsGameClient.java`**：可以改，但只能**增加逻辑**或**修复 bug**，不得删除现有功能或改变现有消息处理的行为。

---

## 验收标准

全部完成后，Cursor 必须产出 `docs/COMPLETION_REPORT.md`（见任务 7），报告中必须证明以下全部成立：

1. `typst compile docs/INTERFACE.typ docs/INTERFACE.pdf` 零错误通过
2. INTERFACE.typ 中老师要求的全部 messageType、错误码、数据结构、通信流程均有文档化，且与代码实现一致
3. `mvn test` 全部模块全绿，包含 ≥ 8 个 WS 集成测试场景（报告粘贴完整输出）
4. 启动 `WsGameServer 8887` → 启动 2 个 `WsAIGameClient` → 自动完成至少一局对弈，棋谱落盘（报告粘贴服务器日志关键行）
5. `docker compose up` 启动的是 WS 8887
6. `README.md` 默认命令指向 `server-ws 8887`
7. 所有已有的 jieqi-core 单元测试（19 类）不受影响（报告附最终统计）
8. TCP 8888 路径仍可正常工作（选项 1-2 未删除）
9. `docs/INTERFACE.typ` 版本号升至 v3.1，版本历史已记录本次变更
10. 协议对照表覆盖老师规范 100% 条目，无遗漏

---

*本文档基于老师 `2026大作业公共接口.docx` + `2026大作业——揭棋.docx` + `CODEBASE_AND_TEACHER_MIGRATION_PLAN.md` 编写。*

# 揭棋对弈 — 公共通信协议 v3.0

> **定位**：对齐课程《2026大作业公共接口》WebSocket + JSON；Typst 权威源文件为 [`INTERFACE.typ`](./INTERFACE.typ)（编译为 `INTERFACE.pdf`）。  
> **参考实现**：`com.jieqi.protocol.json.*`、`WsGameServer`、`WsGameClient`  
> **可选扩展**：TCP 文本帧 v2.0 见 [附录 B（INTERFACE.typ）](#附录-btcp-v20可选)

---

## 目录

1. [基础约定](#1-基础约定)
2. [WebSocket + JSON 消息](#2-websocket--json-消息)
3. [坐标与棋子编码](#3-坐标与棋子编码)
4. [Move 对象与 JSON 映射](#4-move-对象与-json-映射)
5. [典型通信流程](#5-典型通信流程)
6. [揭棋规则要点](#6-揭棋规则要点)
7. [胜负与和棋](#7-胜负与和棋)
8. [组间联调清单](#8-组间联调清单)
9. [附录 B：TCP v2.0（可选）](#附录-btcp-v20可选)

---

## 1. 基础约定

| 项目 | 约定 |
|------|------|
| 传输 | **WebSocket** + **JSON**（UTF-8） |
| 默认端口 | **8887**（`ws://host:8887`） |
| 消息识别 | 每条 JSON 必含 `messageType` 字符串 |
| 心跳 | 可选 `ping` / `pong`，建议 10s |
| 步时 | 课程默认 60s/步；**本组实现 65s**（60+5 网络裕量） |
| 翻子权威 | 服务器随机排布 + `flipResult` 揭示 |

---

## 2. WebSocket + JSON 消息

### 2.1 客户端 → 服务器

| messageType | 字段 |
|-------------|------|
| `Login` | `userId`, `password` |
| `register` | `userId`, `password`, `nickname` |
| `startMatch` | — |
| `cancelMatch` | —（可选） |
| `requestFirstHand` | `wannaFirst`: boolean（可选，10s 内） |
| `Ready` | — |
| `move` | `fromX`, `fromY`, `toX`, `toY`, `isFlip` |
| `ping` | `timestamp`（可选） |
| `Resign` | — |

### 2.2 服务器 → 客户端

| messageType | 字段 |
|-------------|------|
| `loginResult` | `success`, `message`, `userId?` |
| `matchSuccess` | `roomId`, `opponentId`, `opponentNickname` |
| `roomInfo` | `opponentReady` |
| `gameStart` | `redPlayerId`, `blackPlayerId`, `yourColor`, `firstHand`, `initialBoard[]` |
| `moveResult` | `success`, `valid`, `move`, `flipResult?` |
| `timeout` | `loserId`, `winnerId`, `reason` |
| `gameOver` | `winner`, `reason`, `winnerId` |
| `pong` | `timestamp` |
| `error` | `code`, `message` |

**走子广播**：`valid=true` → 双方；`valid=false` → 仅发送方。

### 2.3 initialBoard 单元格

```json
{ "x": "a", "y": 0, "piece": "rook", "visible": false }
```

### 2.4 错误码 error.code

| code | 含义 |
|------|------|
| 1001 | 登录失败 |
| 1002 | 重复登录 |
| 2001 | 非法走子 |
| 2002 | 未轮到本方 |
| 2003 | 超时 |
| 3001 | 房间不存在 |
| 3002 | 匹配失败 |
| 4001 | JSON 格式错误 |

### 2.5 gameOver.reason（含本组扩展）

`checkmate` · `resign` · `timeout` · `stalemate` · `disconnect` · `king_captured` · `draw_no_capture` · `repetition_loss` · `repetition_draw` · `draw_agreed`

---

## 3. 坐标与棋子编码

- 列 `a`–`i`，行 `0`–`9`（0=红方底线，9=黑方底线）
- JSON：`fromX` + `fromY`；内部：`source = fromX + fromY`（如 `"b1"`）

| piece (JSON) | 内部码 | 说明 |
|--------------|--------|------|
| king | 0 | 将/帅，开局即明 |
| rook | 1 | 车 |
| knight | 2 | 马 |
| cannon | 3 | 炮 |
| pawn | 4 | 兵/卒 |
| guard | 5 | 仕/士 |
| bishop | 6 | 相/象 |

颜色：`yourColor` = `"red"` / `"black"`；内部 `0`/`1`。

---

## 4. Move 对象与 JSON 映射

领域层 `Move`（`jieqi-core`）：

```java
Move(source, destination)  // 如 "b1" -> "b3"
move.setFlipOnly(isFlip && source.equals(destination));
```

JSON `move` 示例：

```json
{"messageType":"move","fromX":"b","fromY":1,"toX":"b","toY":3,"isFlip":false}
```

服务器：`JsonMessages.parseMove()` → `RuleValidator` → `RandomRevealService` → `moveResult` + `flipResult`。

---

## 5. 典型通信流程

```
C: Login → S: loginResult
C: startMatch (×2) → S: matchSuccess
C: Ready (×2) → S: roomInfo, gameStart
C: move → S: moveResult (broadcast if valid)
... → S: gameOver / timeout
```

---

## 6. 揭棋规则要点

（与 v2.0 相同，详见 INTERFACE.typ 第五–八章）

- 暗子按**虚拟类型**走子；明士/明象可过河
- 服务器权威翻子；客户端不可伪造
- **允许送将**；对方可吃将，`reason=king_captured`
- 40 回合无吃子 = 80 半步和棋
- 长将/长捉 ≥6 次判负；兵卒长捉和

---

## 7. 胜负与和棋

由服务器 `EndgameJudge` + `Game` 判定，通过 `gameOver` / `timeout` 通知客户端。扩展 reason 字符串见 §2.5。

---

## 8. 组间联调清单

- [x] WebSocket JSON 解析与未知 messageType 容错
- [x] Login → match → Ready → gameStart 流程
- [x] 坐标 fromX/fromY 正确
- [x] flipResult 服务器生成
- [x] initialBoard 重建棋盘
- [x] 非法着法 valid=false 不改变局面
- [x] 断线 gameOver(disconnect)
- [ ] 与他组交叉联调（见 INTEROP.md）

---

## 启动命令

```bash
# WebSocket 服务器（推荐，端口 8887）
mvn exec:java -pl jieqi-server -Dexec.mainClass="com.jieqi.server.ws.WsGameServer"

# WebSocket 客户端
mvn exec:java -pl jieqi-client -Dexec.mainClass="com.jieqi.client.WsGameClient" \
  -Dexec.args="ws://127.0.0.1:8887 player1 123456"

# 编译 PDF
typst compile docs/INTERFACE.typ docs/INTERFACE.pdf
```

---

## 附录 B：TCP v2.0（可选）

本组保留 TCP `msgType|payloadLen|payload\n`（端口 **8888**），实现类 `Protocol.java`、`GameServer`、`GameClient`。

| msgType | 名称 |
|---------|------|
| 1–7 | LOGIN, MOVE, GAME_STATE, ERROR, QUIT, GAME_OVER, BOARD_STATE |
| 8–10 | DRAW_REQUEST, RESIGN, CHAT（可选） |

**联调前须与对方约定使用 WebSocket JSON（正文）或 TCP v2.0（附录），不可混用。**

---

> v3.0 — 2026-05-29：对齐课程公共接口 WebSocket JSON；TCP v2.0 降为可选附录。

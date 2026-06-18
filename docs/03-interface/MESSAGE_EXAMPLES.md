# JSON 消息交互示例

> **权威定义**： [INTERFACE.typ](../INTERFACE.typ) v3.0  
> **归属说明**：标「扩展」的消息为本组增加，不影响与老师客户端必做互操作。

下列 JSON 可直接用于 WebSocket 联调（字段名与代码 `JsonMessageTypes` 一致）。

---

## 1. 登录

**C→S `Login`**

```json
{
  "messageType": "Login",
  "userId": "player1",
  "password": "123456"
}
```

**S→C `loginResult`**

```json
{
  "messageType": "loginResult",
  "success": true,
  "message": "ok",
  "userId": "player1"
}
```

---

## 2. 匹配与准备

**C→S `startMatch`**

```json
{ "messageType": "startMatch" }
```

**S→C `matchSuccess`**

```json
{
  "messageType": "matchSuccess",
  "roomId": "room_a05bcbff",
  "opponentId": "player2",
  "opponentNickname": "player2"
}
```

**C→S `Ready`**

```json
{ "messageType": "Ready" }
```

**C→S `requestFirstHand`**

```json
{
  "messageType": "requestFirstHand",
  "wannaFirst": true
}
```

---

## 3. 开局 `gameStart`

**S→C**（`initialBoard` 为 10×9 单元格数组，此处省略为示意）

```json
{
  "messageType": "gameStart",
  "redPlayerId": "player1",
  "blackPlayerId": "player2",
  "yourColor": "red",
  "firstHand": true,
  "initialBoard": [
    [{"piece":"king","color":"black","revealed":true}, "..."],
    "... 共 10 行 ..."
  ]
}
```

---

## 4. 走子

**C→S `move`**

```json
{
  "messageType": "move",
  "fromX": "b",
  "fromY": 0,
  "toX": "b",
  "toY": 3,
  "isFlip": false
}
```

**S→C `moveResult`（合法，含翻子）**

```json
{
  "messageType": "moveResult",
  "success": true,
  "valid": true,
  "move": {
    "from": "b0",
    "to": "b3",
    "type": "knight"
  },
  "flipResult": "knight"
}
```

**S→C `moveResult`（非法）**

```json
{
  "messageType": "moveResult",
  "success": true,
  "valid": false
}
```

**S→C `error`**

```json
{
  "messageType": "error",
  "code": 2001,
  "message": "非法走法"
}
```

---

## 5. 聊天与心跳

**C→S `chat`**

```json
{
  "messageType": "chat",
  "content": "你好",
  "timestamp": 1712345678901
}
```

**C→S `ping` / S→C `pong`**

```json
{ "messageType": "ping", "timestamp": 1712345678901 }
```

```json
{ "messageType": "pong", "timestamp": 1712345678901 }
```

---

## 6. 提和与认输

**C→S `drawOffer`**

```json
{ "messageType": "drawOffer" }
```

**C→S `Resign`**

```json
{ "messageType": "Resign" }
```

---

## 7. 终局 `gameOver`

```json
{
  "messageType": "gameOver",
  "winner": "red",
  "reason": "checkmate",
  "winnerId": "player1",
  "capturedReveal": [
    { "color": "black", "wasDark": true, "piece": "rook" }
  ]
}
```

`reason` 扩展值见 `INTERFACE.typ`（如 `king_captured`、`repetition_loss`）。

---

## 8. 本组扩展：复盘

**C→S `replayRequest`**

```json
{
  "messageType": "replayRequest",
  "stepIndex": 0
}
```

**S→C `replayFrame`**

```json
{
  "messageType": "replayFrame",
  "roomId": "room_a05bcbff",
  "stepIndex": 3,
  "totalSteps": 48,
  "currentTurn": "black",
  "status": "PLAYING",
  "board": [ "..." ],
  "move": { "from": "a6", "to": "a5" }
}
```

---

## 9. 本组扩展：再来一局 / 人机

**C→S `rematchRequest`**

```json
{ "messageType": "rematchRequest" }
```

**C→S `startAiGame`**

```json
{
  "messageType": "startAiGame",
  "aiLevel": "hard"
}
```

---

## 10. 消息归属速查

| messageType | 归属 |
|-------------|------|
| Login, startMatch, Ready, move, gameStart, moveResult, gameOver, chat | 课程公共接口 |
| replayRequest, replayFrame, rematchRequest, addTime, pauseGame | 本组扩展 |

完整表见 [INTERFACE.typ](../INTERFACE.typ) §本组扩展消息。

---

*示例与 `JsonMessages` 工厂方法对齐 · v1.0 · 2026-06-18*

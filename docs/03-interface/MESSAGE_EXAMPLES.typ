#import "../template.typ": *
#show: doc => [ #cover(title: "JSON 消息交互示例", subtitle: "Message Examples — WebSocket JSON 联调参考", doc-type: "接口文档") #doc ]
#setup-doc(title: "Unveil — 消息示例")

= 登录

*C→S `Login`*

```json
{
  "messageType": "Login",
  "userId": "player1",
  "password": "123456"
}
```

*S→C `loginResult`*

```json
{
  "messageType": "loginResult",
  "success": true,
  "message": "ok",
  "userId": "player1"
}
```

= 匹配与准备

*C→S `startMatch`*

```json
{ "messageType": "startMatch" }
```

*S→C `matchSuccess`*

```json
{
  "messageType": "matchSuccess",
  "roomId": "room_a05bcbff",
  "opponentId": "player2",
  "opponentNickname": "player2"
}
```

*C→S `Ready`*

```json
{ "messageType": "Ready" }
```

*C→S `requestFirstHand`*

```json
{
  "messageType": "requestFirstHand",
  "wannaFirst": true
}
```

= 开局 `gameStart`

*S→C*（`initialBoard` 为 10×9 单元格数组，此处省略为示意）

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

= 走子

*C→S `move`*

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

*S→C `moveResult`（合法，含翻子）*

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

*S→C `moveResult`（非法）*

```json
{
  "messageType": "moveResult",
  "success": true,
  "valid": false
}
```

*S→C `error`*

```json
{
  "messageType": "error",
  "code": 2001,
  "message": "非法走法"
}
```

= 聊天与心跳

*C→S `chat`*

```json
{
  "messageType": "chat",
  "content": "你好",
  "timestamp": 1712345678901
}
```

*C→S `ping` / S→C `pong`*

```json
{ "messageType": "ping", "timestamp": 1712345678901 }
```

```json
{ "messageType": "pong", "timestamp": 1712345678901 }
```

= 提和与认输

*C→S `drawOffer`*

```json
{ "messageType": "drawOffer" }
```

*C→S `Resign`*

```json
{ "messageType": "Resign" }
```

= 终局 `gameOver`

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

= 本组扩展：复盘

*C→S `replayRequest`*

```json
{
  "messageType": "replayRequest",
  "stepIndex": 0
}
```

*S→C `replayFrame`*

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

= 本组扩展：再来一局 / 人机

*C→S `rematchRequest`*

```json
{ "messageType": "rematchRequest" }
```

*C→S `startAiGame`*

```json
{
  "messageType": "startAiGame",
  "aiLevel": "hard"
}
```

= 消息归属速查

#table(
  columns: (auto, auto),
  [*messageType*], [*归属*],
  [Login, startMatch, Ready, move, gameStart, moveResult, gameOver, chat], [课程公共接口],
  [replayRequest, replayFrame, rematchRequest, addTime, pauseGame], [本组扩展],
)

完整表见 `INTERFACE.typ` §本组扩展消息。

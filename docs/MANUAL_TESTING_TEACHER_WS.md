# 手动测试流程：老师 WebSocket + JSON 版本

本文档用于验收课程公共接口版本，也就是老师文档要求的 **WebSocket + JSON** 通信方式。  
默认端口：`8887`。默认入口：`server-ws` / `client-ws`。

所有命令默认在项目根目录执行：

```bash
cd /Users/bosprimigenious/Projects/Study/Unveil
```

## 1. 测试目标

这份流程验证：

- WebSocket 服务端能启动。
- 客户端通过 JSON `messageType` 登录、匹配、准备、开局。
- 服务端发送 `loginResult`、`matchSuccess`、`gameStart`。
- 客户端发送老师格式的 `move`。
- 服务端返回 `moveResult`、`flipResult`。
- `ping` 能返回 `pong`。
- 非法走法能返回 `error` 和无效 `moveResult`。
- 认输、超时、断线能结束游戏。
- 对局结束后能保存棋谱。

## 2. 环境检查

检查 Java：

```bash
java -version
```

预期：

- Java 版本为 21。
- 如果不是 21，先切换 JDK。

检查 Maven：

```bash
mvn -version
```

预期：

- Maven 可用。
- 如果提示 `mvn: command not found`，说明当前终端没有 Maven，需要先安装或配置 Maven。

## 3. 编译与自动测试

完整测试：

```bash
mvn clean test
```

只跑 WebSocket 相关模块：

```bash
mvn test -pl jieqi-core,jieqi-server -am
```

预期：

- `BUILD SUCCESS`
- WebSocket 集成测试通过。

如果遇到依赖下载失败：

- 检查网络。
- 检查 Maven 是否能访问中央仓库。
- 第一次运行需要下载 `Java-WebSocket`、`gson`、JUnit 等依赖。

## 4. 启动 WebSocket 服务器

打开第一个终端：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"
```

看到类似输出：

```text
[WS] 揭棋 WebSocket 服务器已启动，端口: 8887
[WS] 协议: 老师公共接口 JSON (ws://localhost:8887)
[WS] 步时上限: 65000ms
```

注意：

- 这个终端不要关闭。
- 如果端口被占用，换端口，例如 `8897`，客户端也要同步改 URL。

换端口示例：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8897"
```

## 5. 启动两个 WebSocket 客户端

打开第二个终端，启动玩家 1：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"
```

预期看到：

```text
[WS Client] 已连接服务器
[WS Client] 登录成功: player1
命令:
  match
  ready
  first <true|false>
  move <fx> <fy> <tx> <ty> [flip]
  ...
```

打开第三个终端，启动玩家 2：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player2 123456"
```

预期看到：

```text
[WS Client] 已连接服务器
[WS Client] 登录成功: player2
```

注意：

- 两个客户端的 `userId` 不能相同。
- 如果相同，服务端应返回重复登录错误 `1002`。

## 6. 匹配流程

在 player1 客户端输入：

```text
match
```

在 player2 客户端输入：

```text
match
```

预期两个客户端都看到类似：

```text
[WS Client] 匹配成功 room=room_xxxxxxxx 对手=player2 (id=player2)
```

服务端终端看到类似：

```text
[WS] 匹配成功 roomId=room_xxxxxxxx player1 vs player2
```

对应老师 JSON 流程：

```json
{ "messageType": "startMatch" }
```

服务端返回：

```json
{
  "messageType": "matchSuccess",
  "roomId": "room_xxxxxxxx",
  "opponentId": "player2",
  "opponentNickname": "player2"
}
```

## 7. 先手协商与准备

如果不测试先手协商，可以直接双方 `ready`。  
如果要测试先手协商，在 10 秒窗口内输入：

player1：

```text
first true
```

player2：

```text
first false
```

或反过来让 player2 请求先手：

```text
first true
```

然后双方分别输入：

```text
ready
```

预期：

- 对方准备后，本方看到 `对手已准备: true`。
- 双方都准备后，服务端发送 `gameStart`。

客户端看到类似：

```text
[WS Client] 开局 yourColor=red firstHand=true 红方=player1 黑方=player2
```

另一边看到：

```text
[WS Client] 开局 yourColor=black firstHand=false 红方=player1 黑方=player2
```

并显示棋盘。

对应老师 JSON：

```json
{ "messageType": "requestFirstHand", "wannaFirst": true }
```

```json
{ "messageType": "Ready" }
```

服务端返回：

```json
{
  "messageType": "gameStart",
  "redPlayerId": "player1",
  "blackPlayerId": "player2",
  "yourColor": "red",
  "firstHand": true,
  "initialBoard": []
}
```

注意：

- 当前代码里 `initialBoard` 按棋子格子输出，不是强制 90 个格子。若老师严格要求 9x10 每格都出现，这里需要进一步补齐空格。
- 坐标中 `y=0` 是红方底线，`y=9` 是黑方底线；列为 `a-i`。

## 8. 查看棋盘

任一客户端输入：

```text
board
```

或：

```text
b
```

预期：

- 打印当前本地棋盘。
- 棋盘视角跟你的颜色相关。

注意：

- 棋盘同步依赖 `gameStart.initialBoard` 和后续 `moveResult`。
- 如果你看到棋盘没有变化，先确认是否收到了有效 `moveResult`。

## 9. 走子与翻子

WebSocket 客户端走子命令格式：

```text
move <fromX> <fromY> <toX> <toY> [flip]
```

例子：

```text
move a 3 a 4
```

原地翻子：

```text
move a 3 a 3 flip
```

老师 JSON 对应：

```json
{
  "messageType": "move",
  "fromX": "a",
  "fromY": 3,
  "toX": "a",
  "toY": 4,
  "isFlip": true
}
```

预期：

- 合法走法后，双方收到 `moveResult`。
- 如果走的是暗子，服务端返回 `flipResult`，例如 `rook`、`cannon`、`pawn`。
- 两边棋盘同步更新。

客户端可能看到：

```text
[WS Client] 翻出: cannon
```

注意：

- 必须轮到自己时才能走。
- 如果输入坐标不是当前棋盘上的合法着法，会返回错误。
- 当前实现里 `moveResult.move.isFlip` 主要表示原地翻子；暗子移动后翻开会通过 `flipResult` 表示。若老师严格要求这时 `move.isFlip=true`，还需要补一个小修正。

## 10. 非法走法测试

轮到红方时，输入一个明显非法走法：

```text
move a 4 a 5
```

如果该坐标没有棋子或规则不允许，预期看到：

```text
[WS Client] 错误 2001: 非法走法
[WS Client] 着法无效
```

不是自己回合时输入任意走法，预期看到：

```text
[WS Client] 错误 2002: 未轮到本方走子
[WS Client] 着法无效
```

老师错误码对应：

- `2001`：非法走子
- `2002`：未轮到本方走子

## 11. 心跳测试

任一客户端输入：

```text
ping
```

预期看到：

```text
[WS Client] pong ts=...
```

对应老师 JSON：

```json
{ "messageType": "ping", "timestamp": 1712345678901 }
```

服务端返回：

```json
{ "messageType": "pong", "timestamp": 1712345678901 }
```

注意：

- 当前客户端是手动 `ping`。
- 老师文档说 10 秒通信一次，若要演示自动心跳，可以后续在客户端加定时发送。

## 12. 认输测试

任一对局中的客户端输入：

```text
resign
```

预期双方看到：

```text
[WS Client] 对局结束: {"messageType":"gameOver","winner":"black","reason":"resign","winnerId":"player2"}
```

服务端看到类似：

```text
[WS] 对局结束 roomId=room_xxxxxxxx BLACK_WIN
[WS] 棋谱已保存: records/...
```

对应老师 JSON：

```json
{ "messageType": "Resign" }
```

服务端返回：

```json
{
  "messageType": "gameOver",
  "winner": "black",
  "reason": "resign",
  "winnerId": "player2"
}
```

## 13. 超时测试

重新开一局后，让当前回合玩家不操作，等待 65 秒以上。

预期双方先收到：

```json
{
  "messageType": "timeout",
  "loserId": "player1",
  "winnerId": "player2",
  "reason": "timeout"
}
```

随后收到：

```json
{
  "messageType": "gameOver",
  "winner": "black",
  "reason": "timeout",
  "winnerId": "player2"
}
```

注意：

- 步时是 60 秒 + 5 秒网络裕量。
- 服务端每秒检查一次，所以实际可能略大于 65 秒。

## 14. 断线测试

对局开始后，直接关闭其中一个客户端：

```text
quit
```

或用 `Ctrl+C` 停掉客户端。

预期：

- 对手收到 `gameOver`。
- 原因是 `disconnect`。

注意：

- 老师文档没有强制断线重连，断线判负是本组扩展处理。

## 15. 棋谱检查

对局结束后，在项目根目录查看：

```bash
ls records
```

打开某个棋谱：

```bash
cat records/<文件名>
```

预期：

- 能看到本局走子记录。
- 认输、超时等结束场景也应触发保存。

## 16. 可选：原始 JSON 联调

如果安装了 `websocat`，可以不用本仓库客户端，直接发老师 JSON：

```bash
websocat ws://127.0.0.1:8887
```

然后输入：

```json
{"messageType":"Login","userId":"raw1","password":"123456"}
```

预期返回：

```json
{"messageType":"loginResult","success":true,"message":"登录成功","userId":"raw1"}
```

另开一个终端连接第二个用户：

```bash
websocat ws://127.0.0.1:8887
```

输入：

```json
{"messageType":"Login","userId":"raw2","password":"123456"}
```

两个终端分别输入：

```json
{"messageType":"startMatch"}
```

预期双方收到 `matchSuccess`。

再分别输入：

```json
{"messageType":"Ready"}
```

预期双方收到 `gameStart`。

注意：

- `websocat` 不是项目依赖，机器上没有就跳过。
- 也可以用 Postman、Apifox、浏览器 WebSocket 插件做同样测试。

## 17. 常见问题

### 17.1 Maven 不存在

现象：

```text
mvn: command not found
```

处理：

- 安装 Maven。
- 或在 IDE 中用 Maven 面板运行。

### 17.2 端口占用

现象：

```text
Address already in use
```

处理：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8897"
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8897 player1 123456"
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8897 player2 123456"
```

### 17.3 客户端连不上

现象：

```text
WebSocket 客户端错误
```

检查：

- 服务器是否已启动。
- URL 是否为 `ws://127.0.0.1:8887`。
- 如果跨机器，是否把 `127.0.0.1` 换成服务器 IP。
- 防火墙是否放行端口。

### 17.4 一直没有开局

检查：

- 两个客户端是否都输入了 `match`。
- 两个客户端是否都输入了 `ready`。
- 是否有一方断线。
- 是否正在等待 10 秒先手协商窗口。

### 17.5 走子一直非法

处理：

- 先输入 `board` 看棋盘。
- 确认轮到自己。
- 确认坐标格式是 `move a 3 a 4`，不是 `a3 a4`。
- 确认 `y=0` 是红方底线，`y=9` 是黑方底线。

## 18. 最小验收清单

提交或演示前至少完成：

- `mvn test -pl jieqi-core,jieqi-server -am` 通过。
- WebSocket 服务端能在 `8887` 启动。
- 两个 WebSocket 客户端能登录成功。
- 两个客户端能 `match` 并收到 `matchSuccess`。
- 两个客户端能 `ready` 并收到 `gameStart`。
- `gameStart` 含 `redPlayerId`、`blackPlayerId`、`yourColor`、`firstHand`、`initialBoard`。
- 合法走子能收到 `moveResult`。
- 暗子翻开能收到 `flipResult`。
- 非法走法能收到 `error.code=2001`。
- 非本方回合走子能收到 `error.code=2002`。
- `ping` 能收到 `pong`。
- `resign` 能收到 `gameOver.reason=resign`。
- 超时能收到 `timeout` 和 `gameOver.reason=timeout`。
- `records/` 下能看到棋谱文件。

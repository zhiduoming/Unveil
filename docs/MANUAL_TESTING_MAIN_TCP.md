# 手动测试流程：main 分支 TCP 版本

本文档用于测试 `main` 分支原有的 TCP 文本帧版本。  
该版本不是老师 WebSocket + JSON 主协议，而是本组早期实现的 TCP 调试协议：

```text
msgType|payloadByteLength|payload\n
```

默认端口：`8888`。默认入口：`server` / `client`。

所有命令默认在项目根目录执行：

```bash
cd /Users/bosprimigenious/Projects/Study/Unveil
```

## 1. 测试目标

这份流程验证：

- TCP 服务器能启动。
- 两个 TCP 客户端能连接并自动匹配。
- 双方能显示棋盘。
- 能走子、翻子、聊天。
- 非法走法能被拒绝。
- 提和、认输、超时能结束游戏。
- 对局结束后能保存棋谱。

注意：

- 这个版本适合测试本组客户端和服务器闭环。
- 如果老师或别组按 JSON `messageType` 来连，这个 TCP 版本不能直接兼容。
- 若要按老师公共接口验收，请看 `docs/MANUAL_TESTING_TEACHER_WS.md`。

## 2. 切换到 main 分支

如果当前不在 `main`，先确认没有未提交改动：

```bash
git status --short --branch
```

然后切换：

```bash
git checkout main
```

预期：

```text
Switched to branch 'main'
```

如果提示有未提交改动：

- 先提交。
- 或者确认这些改动不需要后再处理。
- 不要直接强制覆盖，避免丢代码。

## 3. 环境检查

检查 Java：

```bash
java -version
```

预期：

- Java 版本为 21。

检查 Maven：

```bash
mvn -version
```

预期：

- Maven 可用。

如果提示：

```text
mvn: command not found
```

说明当前终端没有 Maven，需要先安装或配置 Maven。

## 4. 编译与自动测试

完整测试：

```bash
mvn clean test
```

快速编译：

```bash
mvn clean compile
```

预期：

```text
BUILD SUCCESS
```

如果只想测试服务端和核心模块：

```bash
mvn test -pl jieqi-core,jieqi-server -am
```

## 5. 启动 TCP 服务器

打开第一个终端：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8888"
```

如果当前分支支持 `-f jieqi-app/pom.xml -am` 写法，也可以用：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server 8888"
```

预期看到：

```text
揭棋服务器启动，监听端口: 8888
默认超时阈值: 65000ms (60000ms + 5000ms 网络裕量)
```

注意：

- 服务器终端不要关闭。
- 如果端口占用，换成 `8898` 等其他端口，客户端也要同步修改。

换端口示例：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8898"
```

## 6. 启动两个 TCP 客户端

打开第二个终端，启动玩家 Alice：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8888 Alice"
```

客户端会询问：

```text
请选择颜色 (0=红方/先手, 1=黑方/后手):
```

输入：

```text
0
```

然后询问：

```text
输入游戏ID (留空自动匹配, new=新建房间):
```

直接回车。

打开第三个终端，启动玩家 Bob：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8888 Bob"
```

颜色输入：

```text
1
```

游戏 ID 直接回车。

预期：

- 两边都显示加入游戏。
- 两边都看到 `游戏开始`。
- 两边都显示棋盘。

## 7. TCP 客户端命令列表

进入游戏后，在 `>` 后输入命令。

显示棋盘：

```text
board
```

简写：

```text
b
```

正常走子：

```text
b1 b3
```

格式：

```text
<源坐标> <目标坐标>
```

原地翻子：

```text
flip a0
```

简写：

```text
f a0
```

聊天：

```text
chat 你好
```

简写：

```text
c 你好
```

提和：

```text
draw
```

简写：

```text
d
```

认输：

```text
resign
```

简写：

```text
r
```

退出：

```text
quit
```

或：

```text
exit
```

帮助：

```text
help
```

## 8. 基础联通测试

流程：

1. 启动服务器。
2. 启动 Alice，颜色输入 `0`，游戏 ID 留空。
3. 启动 Bob，颜色输入 `1`，游戏 ID 留空。
4. 两边输入：

```text
board
```

预期：

- 两边都能显示棋盘。
- 服务器终端显示两个客户端连接。
- 双方进入同一个 `gameId`。

注意：

- 如果一直等待对手，检查两个客户端端口是否一致。
- 如果填了不同 gameId，可能不会进入同一局。

## 9. 翻子测试

轮到红方时，输入一个红方暗子坐标，例如：

```text
flip a0
```

如果提示非法，先输入：

```text
board
```

换一个当前棋盘上属于自己的暗子坐标。

预期：

- 本地不再提示非法。
- 双方都收到走法广播。
- 两边棋盘同步更新。

注意：

- 不是所有坐标都有棋子。
- 不能翻对方棋子。
- 已经明子的格子不能再次翻。

## 10. 走子测试

轮到自己时，输入合法走法，例如：

```text
b0 b1
```

具体坐标以当前棋盘为准。

预期：

- 走子方发送成功。
- 对方收到走法。
- 两边棋盘变化一致。
- 回合切换提示正常。

如果看到：

```text
[本地校验] 非法着法，未发送
```

说明客户端本地规则校验不通过。换一个合法走法。

## 11. 非法走法测试

输入明显非法的走法：

```text
a0 z9
```

或在不是自己回合时走子。

预期：

- 客户端可能本地拦截：

```text
[本地校验] 非法着法，未发送
```

- 如果消息发到服务器，服务器返回错误，例如：

```text
[错误 101] 非法走法
```

或：

```text
[错误 107] 不是你的回合
```

注意：

- TCP 版本错误码是本组自定义协议，不是老师 JSON 错误码。
- 老师 JSON 错误码请看 WebSocket 版本文档。

## 12. 聊天测试

Alice 输入：

```text
chat hello from Alice
```

Bob 预期看到：

```text
[Alice(红方)]: hello from Alice
```

Bob 输入：

```text
chat hello from Bob
```

Alice 预期看到对方消息。

注意：

- TCP 版本聊天有 10 秒限速。
- 如果发太快，可能看到聊天过于频繁的错误。

## 13. 提和测试

Alice 输入：

```text
draw
```

Bob 预期看到：

```text
对方提和。输入 'draw' 同意，或继续走子拒绝。
```

Bob 输入：

```text
draw
```

预期：

- 双方收到游戏结束。
- 结果是和棋。
- 服务端保存棋谱。

注意：

- TCP 版本提和是本组扩展消息。
- 老师原始文档没有把提和列为核心必选项。

## 14. 认输测试

重新开一局后，任一方输入：

```text
resign
```

预期：

- 双方收到游戏结束。
- 认输方判负。
- 服务端保存棋谱。

客户端可能看到：

```text
红方已认输
游戏结束：你赢了！
原因: 认输
```

## 15. 超时测试

重新开一局后，轮到某一方时不要输入任何走法，等待 65 秒以上。

预期：

- 服务器判当前回合方超时负。
- 双方收到游戏结束。
- 服务端保存棋谱。

注意：

- 步时为 60 秒 + 5 秒网络裕量。
- 服务端每秒检查一次，所以可能略大于 65 秒。

## 16. 指定房间测试

Alice 启动客户端后：

颜色输入：

```text
0
```

游戏 ID 输入：

```text
new
```

预期看到：

```text
已加入游戏: <gameId>，颜色: 红方，状态: WAITING
```

Bob 启动客户端后：

颜色输入：

```text
1
```

游戏 ID 输入 Alice 控制台里的 `<gameId>`。

预期：

- Bob 加入 Alice 的房间。
- 双方开局。

注意：

- gameId 复制错会返回房间不存在。
- 对局结束后房间会从内存移除。

## 17. 打包运行

如果不想每次用 Maven exec，可以先打包：

```bash
mvn package -pl jieqi-app -am
```

然后启动菜单：

```bash
java -jar jieqi-app/target/unveil-jieqi.jar
```

或直接启动 TCP 服务器：

```bash
java -jar jieqi-app/target/unveil-jieqi.jar server 8888
```

启动 TCP 客户端：

```bash
java -jar jieqi-app/target/unveil-jieqi.jar client 127.0.0.1 8888 Alice
```

## 18. 棋谱检查

对局正常结束后，服务器会把棋谱保存到：

```text
records/
```

查看：

```bash
ls records
```

打开某个棋谱：

```bash
cat records/<文件名>
```

预期：

- 能看到走子记录。
- 文件存在说明棋谱落盘功能正常。

## 19. 常见问题

### 19.1 端口被占用

现象：

```text
服务器启动失败: Address already in use
```

处理：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8898"
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8898 Alice"
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8898 Bob"
```

### 19.2 客户端无法连接

现象：

```text
无法连接服务器
```

检查：

- 服务器是否已经启动。
- 客户端端口是否和服务器一致。
- 跨机器联调时 IP 是否填对。
- 防火墙是否放行端口。

### 19.3 一直等待对手

检查：

- 第二个客户端是否已经进入。
- 两个客户端是否填了不同游戏 ID。
- 是否一个客户端选择了 `new`，另一个却留空进入其他房间。

### 19.4 本地校验非法

现象：

```text
[本地校验] 非法着法，未发送
```

处理：

- 输入 `board` 看棋盘。
- 确认轮到自己。
- 确认源坐标有自己的棋子。
- 尝试 `flip <coord>` 翻开自己的暗子。

### 19.5 老师 JSON 客户端连不上

这是预期限制。TCP 版本不是老师 WebSocket + JSON 协议。  
老师 JSON 客户端应连接：

```text
ws://127.0.0.1:8887
```

并使用 `server-ws` 启动的服务端。

## 20. 最小验收清单

TCP 版本至少完成：

- `mvn clean test` 通过。
- TCP 服务器能在 `8888` 启动。
- 两个 TCP 客户端能连接。
- 两个客户端能自动匹配并开局。
- `board` 能显示棋盘。
- 至少完成一次翻子。
- 至少完成一次合法走子。
- 非法走法能被拒绝。
- 聊天能广播。
- 提和或认输能正常结束游戏。
- 超时能判负。
- `records/` 下能看到棋谱文件。

最终交作业注意：

- 若老师按公共接口验收，请优先演示 WebSocket + JSON 版本。
- TCP 版本可以作为本组功能闭环和调试补充，不应作为唯一验收入口。

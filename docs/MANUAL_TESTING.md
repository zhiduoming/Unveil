# 手动测试流程

本文档从零开始说明如何在命令行里启动、联调、下棋和检查结果。默认在项目根目录执行命令：

```bash
cd /Users/bosprimigenious/Projects/Study/Unveil
```

## 1. 环境准备

确认 Java 和 Maven 可用：

```bash
java -version
mvn -version
```

项目要求：

- JDK 21
- Maven 3.9+

如果 Java 版本不是 21，需要先切换 JDK。

## 2. 编译与自动测试

第一次手动测试前，建议先跑完整测试：

```bash
mvn clean test
```

如果只想快速确认能编译：

```bash
mvn clean compile
```

也可以运行仓库自检脚本：

```bash
powershell -File scripts/verify.ps1
```

如果 macOS 没有 `powershell`，直接用 `mvn clean test` 即可。

## 3. 启动服务器

打开第一个终端，启动服务器：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8888"
```

看到类似输出即可：

```text
揭棋服务器启动，监听端口: 8888
默认超时阈值: 65000ms (60000ms + 5000ms 网络裕量)
```

这个终端不要关闭，服务器会一直运行。

## 4. 启动两个客户端

再打开第二个终端，启动红方客户端：

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

接着询问：

```text
输入游戏ID (留空自动匹配, new=新建房间):
```

第一次测试建议直接回车，表示自动匹配。

然后打开第三个终端，启动黑方客户端：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8888 Bob"
```

颜色输入：

```text
1
```

游戏 ID 也直接回车。

两个客户端都进入后，会看到游戏开始、棋盘显示和当前回合提示。

## 5. 对局内命令

客户端进入游戏后，在 `>` 后输入命令。

显示棋盘：

```text
board
```

也可以简写：

```text
b
```

正常走子：

```text
b1 b3
```

格式是：

```text
<源坐标> <目标坐标>
```

原地翻子：

```text
flip a0
```

也可以简写：

```text
f a0
```

聊天：

```text
chat 你好
```

也可以简写：

```text
c 你好
```

提和：

```text
draw
```

也可以简写：

```text
d
```

对方收到提和后，如果也输入 `draw`，则同意和棋。

认输：

```text
resign
```

也可以简写：

```text
r
```

退出客户端：

```text
quit
```

或：

```text
exit
```

查看帮助：

```text
help
```

## 6. 推荐手动测试顺序

### 6.1 基础联通

1. 启动服务器。
2. 启动 Alice，选择红方 `0`，游戏 ID 留空。
3. 启动 Bob，选择黑方 `1`，游戏 ID 留空。
4. 两边确认收到 `游戏开始` 和棋盘。
5. 两边分别输入 `board`，确认棋盘能显示。

### 6.2 翻子测试

红方输入：

```text
flip a0
```

观察：

- 红方本地没有报非法。
- 黑方也收到走法广播。
- 两边棋盘同步更新。
- 服务器没有异常。

如果 `a0` 当前不是红方能操作的棋子位置，可换一个棋盘上红方暗子坐标。

### 6.3 走子测试

轮到某一方时，输入一个合法走法，例如：

```text
b0 b1
```

具体坐标以当前棋盘为准。若提示 `[本地校验] 非法着法，未发送`，说明该走法不符合规则，换一个合法走法即可。

观察：

- 走子方发送成功。
- 对方收到走法。
- 两边棋盘一致。
- 回合切换提示正常。

### 6.4 非法走法测试

在任意客户端输入明显非法的走法，例如：

```text
a0 z9
```

或尝试在不是自己回合时走子。

预期：

- 客户端本地拦截非法走法，显示 `[本地校验] 非法着法，未发送`。
- 若消息到达服务器，服务器应返回错误消息，例如 `[错误 101]` 或 `[错误 107]`。

### 6.5 聊天测试

Alice 输入：

```text
chat hello from Alice
```

Bob 应看到聊天消息。

Bob 输入：

```text
chat hello from Bob
```

Alice 应看到聊天消息。

### 6.6 提和测试

Alice 输入：

```text
draw
```

Bob 应看到对方提和提示。

Bob 输入：

```text
draw
```

预期：

- 双方收到游戏结束。
- 结束原因是和棋。
- 服务器保存棋谱。

### 6.7 认输测试

重新开一局后，任意一方输入：

```text
resign
```

预期：

- 双方收到游戏结束。
- 认输方判负。
- 服务器保存棋谱。

### 6.8 超时测试

重新开一局后，轮到某一方时不输入任何走法，等待 65 秒以上。

预期：

- 服务器判当前回合方超时负。
- 双方收到游戏结束。
- 服务器保存棋谱。

## 7. 指定房间测试

如果想测试指定房间或多盘对局，可以使用游戏 ID。

客户端登录时：

- 输入 `new`：强制新建房间。
- 输入空字符串：自动匹配。
- 输入具体房间 ID：加入指定房间。

流程：

1. Alice 启动后颜色输入 `0`。
2. 游戏 ID 输入：

```text
new
```

3. Alice 控制台会显示类似：

```text
已加入游戏: xxxx，颜色: 红方，状态: WAITING
```

4. Bob 启动后颜色输入 `1`。
5. 游戏 ID 输入 Alice 控制台里的 `xxxx`。
6. 双方应进入同一局。

## 8. 本地人机与 AI 测试

启动交互菜单：

```bash
mvn exec:java -pl jieqi-app
```

然后按提示选择：

- `3`：AI vs AI 自动对弈
- `4`：人 vs AI 对弈
- `5`：AI 性能测试
- `6`：本地测试模式

也可以直接运行完整测试覆盖 AI 模块：

```bash
mvn test -pl jieqi-ai -am
```

## 9. 打包运行

如果不想每次用 Maven exec，也可以先打包：

```bash
mvn package -pl jieqi-app -am
```

然后运行 fat jar：

```bash
java -jar jieqi-app/target/unveil-jieqi.jar
```

进入菜单后按提示选择服务器、客户端或 AI 模式。

## 10. 棋谱检查

对局正常结束后，服务器会把棋谱保存到：

```text
records/
```

可以查看：

```bash
ls records
```

打开某个棋谱文件：

```bash
cat records/<文件名>
```

棋谱存在并且包含走子记录，说明服务器落盘功能正常。

## 11. 常见问题

端口被占用：

```text
服务器启动失败: Address already in use
```

解决方式：换一个端口，例如：

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8899"
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8899 Alice"
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8899 Bob"
```

客户端无法连接：

```text
无法连接服务器
```

检查：

- 服务器是否已经启动。
- 客户端端口是否和服务器一致。
- 跨机器联调时，IP 是否填对，防火墙是否放行端口。

一直等待对手：

- 第二个客户端还没有进入。
- 两个客户端填了不同的游戏 ID。
- 指定房间 ID 填错。

提示本地非法：

```text
[本地校验] 非法着法，未发送
```

说明客户端规则校验不通过。先输入 `board` 看棋盘，再换一个合法坐标或使用 `flip <coord>`。

## 12. 最小验收清单

提交或演示前，建议至少完成以下项目：

- `mvn clean test` 通过。
- 服务器可在 8888 启动。
- 两个客户端可自动匹配并开局。
- `board` 能显示棋盘。
- 至少完成一次翻子。
- 至少完成一次合法走子。
- 非法走法能被拒绝。
- 聊天能广播。
- 提和或认输能正常结束游戏。
- `records/` 下能看到棋谱文件。
- AI 模块测试 `mvn test -pl jieqi-ai -am` 通过。

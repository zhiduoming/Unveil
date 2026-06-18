#import "../template.typ": *
#show: doc => [ #cover(title: "构建与运行", subtitle: "Build & Run Guide — 环境、构建、演示流程", doc-type: "工程交付") #doc ]
#setup-doc(title: "Unveil — 构建与运行指南")

= 环境要求

- *JDK 21*：`java -version` 确认
- *Maven 3.9+*：`mvn -version` 确认
- Windows / macOS / Linux 均可

= 构建

```bash
mvn clean package -DskipTests
```

= 运行模式

#table(
  columns: (auto, auto, auto),
  [*模式*], [*命令*], [*预期行为*],
  [交互菜单], [`mvn exec:java -f jieqi-app/pom.xml -am`], [显示 1-9 菜单],
  [WS 服务器], [`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`], [监听 8887 端口],
  [WS 客户端], [`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"`], [连接并登录],
  [AI 自动对弈], [`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="ai-ws ws://127.0.0.1:8887 ai_bot_1 pw123"`], [自动走子],
  [TCP 服务器], [`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server 8888"`], [监听 8888],
  [TCP 客户端], [`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client 127.0.0.1 8888 player1 123456"`], [连接],
  [Fat JAR], [`java -jar jieqi-app/target/unveil-jieqi.jar`], [单 JAR 启动],
)

= 完整演示流程

```text
终端 1: mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"
终端 2: mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"
  > login
  > match
  > ready
  > first true
终端 3: mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player2 123456"
  > login
  > match
  > ready
  > first false
# 对局中
  > move b 0 b 3
  > ai medium
  > replay
  > rematch
```

= 自检

```powershell
powershell -File scripts/verify.ps1
```

= Docker 部署

```bash
docker compose up --build
# 端口 8887（WS）、8888（TCP）映射
```

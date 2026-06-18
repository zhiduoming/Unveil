#import "../template.typ": *
#show: doc => [ #cover(title: "常见问题排查", subtitle: "Troubleshooting — 15 类常见问题与解决方案", doc-type: "工程交付") #doc ]
#setup-doc(title: "Unveil — 常见问题排查")

= 问题速查表

#table(
  columns: (auto, auto, auto, auto),
  [*No.*], [*问题现象*], [*可能原因*], [*解决方案*],
  [1], [启动报 Address already in use], [上次进程未关闭 / 其他程序占用], [netstat -ano | findstr 8887 查 PID 后 kill],
  [2], [Connection refused], [服务器未启动 / 地址错误 / 防火墙], [确认 URL 为 `ws://127.0.0.1:8887`],
  [3], [release version 21 not supported], [JAVA_HOME 指向 JDK 17 或更低], [安装 JDK 21，设置 JAVA_HOME],
  [4], [Unsupported class file major version], [低版本 Java 运行高版本 class], [java -version 必须为 21],
  [5], [verify.ps1 失败], [单元测试未通过或编译错误], [单独运行 mvn test 查看 surefire-reports],
  [6], [AI 走子超时], [搜索深度过大 / 时间预算不足], [选 Easy 模式；检查 AiConfig 时间预算],
  [7], [双方棋盘不同步], [客户端未正确处理 moveResult], [以服务器 moveResult 为准更新本地棋盘],
  [8], [非法走法未被拒绝], [本地模式测试而非服务器模式], [以服务器校验为准；WS 客户端验证],
  [9], [mvn exec:java 行为不符预期], [缓存旧 SNAPSHOT], [mvn install -pl jieqi-app -am -DskipTests],
  [10], [Docker 容器客户端连不上], [端口未映射 / 地址错误], [docker compose ps；确认 8887:8887],
  [11], [复盘 replay 无响应], [对局未结束 / 房间已清理], [仅 gameOver 后使用；确认 .replay.json 存在],
  [12], [匹配后无法开局], [双方未 ready；未争先手], [依次 match → ready → first true/false],
  [13], [Maven 下载依赖极慢], [网络或镜像源问题], [配置国内 Maven 镜像],
  [14], [demo.ps1 窗口闪退], [Maven 未安装 / 路径错误], [手动在根目录 mvn compile],
  [15], [组间联调消息格式不一致], [混用协议；messageType 大小写错误], [统一使用 INTERFACE.typ v3.0],
)

= 分类详解

== 网络与端口

检查清单：
- 服务器终端无异常退出
- 客户端 URL 含 `ws://` 协议头
- 端口号与服务器一致（默认 8887）
- 同一台机器联调使用 `127.0.0.1`

== 构建与 JDK

#table(
  columns: (auto, auto, auto),
  [*检查项*], [*命令*], [*期望*],
  [Java 运行时], [`java -version`], [21.x],
  [Maven JDK], [`mvn -version`], [Java version: 21],
  [JAVA_HOME], [`echo $env:JAVA_HOME`], [指向 JDK 21 根目录],
  [父 POM 编译级别], [查看根 pom.xml], [maven.compiler.release=21],
)

== 测试失败

+ 运行 `mvn test -pl jieqi-core` 定位规则类失败。
+ 运行 `mvn test -pl jieqi-ai` 定位 AI 类失败。
+ 打开 `jieqi-*/target/surefire-reports/TEST-*.xml` 查看 `<failure>` 节点。
+ 修复后重新执行 `powershell -File scripts/verify.ps1`。

== AI 相关

#table(
  columns: (auto, auto, auto),
  [*现象*], [*说明*], [*建议*],
  [Medium 思考 5s+], [迭代加深至预算耗尽], [验收可改用 Easy],
  [Hard 棋力不如 Medium], [Belief 采样过多导致单次搜索过浅], [已调参：约 24 次搜索/步],
  [AI 出非法着法], [搜索回滚不完整或 fallback 失效], [查看 AiBot 最终是否经 generateLegalMoves 校验],
)

== 协议与互操作

- *权威规范*：`INTERFACE.typ` v3.0，冲突时以 Typst 为准。
- *公共消息*：Login、startMatch、Ready、move、gameStart、moveResult、gameOver。
- *本组扩展*：replayRequest、replayFrame、rematchRequest 等。
- *不可混用*：同一对局中不能一端 WS、一端 TCP。

= 日志与诊断命令

#table(
  columns: (auto, auto),
  [*目的*], [*命令*],
  [全量测试], [`mvn test`],
  [单模块测试], [`mvn test -pl jieqi-core`],
  [编译不测试], [`mvn compile`],
  [检查端口 (Windows)], [`netstat -ano | findstr 8887`],
  [协议 PDF], [`typst compile INTERFACE.typ INTERFACE.pdf`],
)

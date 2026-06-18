#import "../template.typ": *
#show: doc => [ #cover(title: "答辩演示脚本", subtitle: "6-8 分钟现场演示时间线", doc-type: "答辩材料") #doc ]
#setup-doc(title: "Unveil — 演示脚本")

= 演示前检查

- JDK 21：`java -version`
- 依赖已构建：`mvn package -pl jieqi-app -am -DskipTests`
- 端口 8887 空闲
- 三分屏窗口或 `scripts/demo.ps1`

= 时间线（6-8 分钟）

#table(
  columns: (auto, auto, auto, auto),
  [*时间*], [*操作*], [*说词*], [*预期结果*],
  [0:00], [运行 verify.ps1], ["首先运行自检脚本，编译、测试、打包全通过"], [OK: verify passed],
  [0:30], [可选展示菜单], ["项目提供 1-9 模式统一入口"], [菜单显示],
  [1:00], [终端1：启动 WS 服务器], ["启动 WebSocket 服务器，端口 8887"], [监听中],
  [1:30], [终端2：玩家一登录], ["第一位玩家连接认证"], [登录成功],
  [2:00], [终端3：玩家二登录], ["第二位玩家连接"], [登录成功],
  [2:30], [match ready first], ["匹配、准备、协商先手"], [gameStart + 棋盘],
  [3:00], [走马：move b 0 b 3], ["演示合法走子与翻子"], [双方同步],
  [3:30], [发非法走法], ["服务器拒绝非法着法"], [valid=false],
  [4:00], [AI：ai medium], ["三档 AI：Easy/Medium/Hard"], [AI 5s 内走子],
  [5:00], [触发终局], ["将死/认输/超时"], [gameOver + 摘要],
  [5:30], [replay 复盘], ["回放棋盘快照时间线"], [逐步回看],
  [6:00], [总结], ["规则、网络、AI、复盘、工程化五位一体"], [—],
)

= 关键命令备忘

```text
# 自检
powershell -File scripts/verify.ps1

# 服务器
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"

# 客户端
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"

# 对局
match → ready → first → move b 0 b 3 → replay
```

= 风险预案

#table(
  columns: (1fr, 1fr, 1fr),
  [*风险*], [*备选*], [*话术*],
  [Maven 下载慢], [用预构建 Fat JAR], ["构建已完成，直接启动产物"],
  [网络卡住], [改本地人机菜单6], ["网络环节展示协议设计"],
  [AI 思考久], [切 ai easy], ["Easy 毫秒响应，Hard 展示算法"],
  [verify 失败], [展示 mvn-test-output.txt], ["今早全量测试已通过"],
)

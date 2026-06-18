#import "../template.typ": *
#show: doc => [ #cover(title: "组间联调记录", subtitle: "Interoperability — 协议联调步骤与记录", doc-type: "接口文档") #doc ]
#setup-doc(title: "Unveil — 组间联调记录")

= 联调前自检

```bash
powershell -File scripts/verify.ps1
```

对照 `INTERFACE.typ` 组间联调清单章节。

= WebSocket 联调步骤（推荐）

+ 约定使用 *WebSocket JSON*（INTERFACE v3.0 正文）与端口 *8887*。
+ A 组起 server：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`
+ B 组起 client：`mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://<A组IP>:8887 userB pass"`
+ 双方：`match` → `ready` → 走子 3～5 步，验证 `moveResult` / `gameStart.initialBoard`。
+ 测试非法着法（`valid=false`）、认输（`gameOver`）、可选 `ping`。

= TCP 扩展联调（附录 B，可选）

#table(
  columns: (auto, auto),
  [*项目*], [*约定*],
  [端口], [8888],
  [帧格式], [msgType|payloadLen|payload\n],
  [多盘], [LOGIN 第三段 gameId（空=匹配）],
)

= 联调记录表

#table(
  columns: (auto, auto, auto, auto, auto),
  [*日期*], [*对方小组*], [*协议*], [*结果*], [*问题与处理*],
  [], [], [], [], [],
)

= 本组自检结果

== WebSocket JSON（v3.0）

#table(
  columns: (auto, auto),
  [*项目*], [*结果*],
  [Login / loginResult], [通过],
  [startMatch / matchSuccess], [通过],
  [Ready / gameStart], [通过],
  [move / moveResult + flipResult], [通过],
  [ping / pong], [通过],
  [非法着法 error], [通过],
  [集成测试], [WsGameServerIntegrationTest 20 场景通过],
  [与他组交叉验证], [待填],
)

== TCP v2.0（附录 B）

#table(
  columns: (auto, auto),
  [*项目*], [*结果*],
  [FrameDecoder / BOARD_STATE], [通过],
  [GameServer 集成测试], [9 项通过],
  [与他组交叉验证], [待填],
)

= 已知差异

见 `INTERFACE.typ` 第十章 Q1–Q44。本组扩展 `gameOver.reason` 字符串（如 `king_captured`）需在联调时说明。

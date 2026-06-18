// Unveil — 项目总览
#import "../template.typ": *

#show: doc => [ #cover(
  title: "项目总览",
  subtitle: "Project Overview — 3 分钟了解全貌",
  doc-type: "验收报告",
) #doc ]

#setup-doc(title: "Unveil — 揭棋对弈程序 · 项目总览")

= 文档导航

#table(
  columns: (1fr, 1fr),
  [*类别*], [*文档*],
  [功能完成度], [FEATURE_MATRIX.md],
  [术语表], [GLOSSARY.md],
  [需求追踪], [REQUIREMENTS.md],
  [总体架构], [ARCHITECTURE.md],
  [规则引擎], [RULE_ENGINE_DESIGN.md],
  [AI 算法], [AI_DESIGN.md],
  [复盘设计], [REPLAY_DESIGN.md],
  [协议规范], [INTERFACE.typ],
  [构建运行], [BUILD_AND_RUN.md],
  [测试用例], [TEST_CASES.md],
  [测试报告], [TEST_REPORT.md],
  [演示脚本], [DEMO_SCRIPT.md],
)

= 项目名称与代号

*Unveil*（揭棋对弈程序）—— 北京邮电大学「揭棋对弈程序设计」课程大作业，#metadata.team（组长：张恒基）。

= 项目背景

揭棋是中国象棋变体：开局仅将/帅明置，其余棋子暗置并按*所在位置的原始角色*走子；首次移动或吃子后随机翻开，明子按真实身份走子。规则涉及暗子/明子差异、强化士象、禁送将、将帅照面、长将长捉等复杂约束。

本系统面向课程验收，提供：*服务端权威规则校验*、*WebSocket + JSON 网络对弈*（课程公共接口，端口 8887）、*三档 AI 对手*、*棋谱记录与复盘追溯*，以及 Maven 多模块工程化交付。

= 核心功能（6 项）

#table(
  columns: (auto, auto),
  [*No.*], [*功能*], [*说明*],
  [1], [揭棋规则引擎], [`Board` / `RuleValidator` / `EndgameJudge`，覆盖走子、翻子、终局判定],
  [2], [WebSocket 真人对弈], [`WsGameServer` + JSON 协议，匹配、房间、步时 65s、全局广播],
  [3], [TCP 兼容通道], [附录 B 文本帧协议，端口 8888，供联调与 legacy 客户端],
  [4], [三档 AI 博弈], [Easy 规则启发 / Medium Alpha-Beta / Hard Belief Sampling + 搜索],
  [5], [棋谱与复盘], [文字棋谱落盘、内存复盘时间线、`replay.json` 持久化与协议回放],
  [6], [Maven 多模块工程], [core / server / client / ai / app 分层，Fat JAR 与自检脚本],
)

= 技术栈

*Java 21* · *Maven* · *Java-WebSocket* · *Gson* · *JUnit 5*

= 项目规模

#table(
  columns: (auto, auto, auto),
  [*指标*], [*数值*], [*说明*],
  [Maven 模块], [*5*], [jieqi-core / jieqi-server / jieqi-client / jieqi-ai / jieqi-app],
  [Java 源文件], [*63*], [主代码 `src/main/java`，不含 `target/`],
  [代码行数], [*~8 500*], [主代码 LOC（`wc -l` 实测为 8 491）],
)

= 项目结构

```text
Unveil/
├── pom.xml                 # 父 POM，聚合 5 模块
├── jieqi-core/             # 领域：棋盘、规则、终局、棋谱、JSON/TCP 协议
├── jieqi-server/           # WebSocket + TCP 服务器、房间、匹配、持久化
├── jieqi-client/           # WebSocket + TCP 控制台客户端
├── jieqi-ai/               # Alpha-Beta、评估、Belief、三档 Bot、Agent 编排
├── jieqi-app/              # 统一启动入口（菜单 / CLI 参数 / Fat JAR）
├── scripts/                # verify.ps1、demo.ps1、run-app.ps1 等
└── docs/                   # 需求、设计、协议、测试、答辩材料
    ├── 00-overview/        # 总览、矩阵、术语
    ├── 02-design/          # 架构与专项设计
    ├── 03-interface/       # INTERFACE.typ（权威协议 v3.0）
    └── 05-testing/         # 测试用例与报告
```

= 演示入口

#table(
  columns: (auto, auto, auto),
  [*方式*], [*命令 / 脚本*], [*预期行为*],
  [*一键演示（推荐）*], [`powershell -File scripts/demo.ps1`], [自动打开 3 个窗口],
  [交互菜单], [`mvn exec:java -f jieqi-app/pom.xml -am`], [数字菜单：服务器 / 客户端 / 人机 / AI 自动对弈],
  [Fat JAR], [`java -jar jieqi-app/target/unveil-jieqi.jar server-ws 8887`], [打包后单 JAR 启动],
)

= 与课程要求的对应

#table(
  columns: (auto, auto, auto),
  [*课程要求*], [*本项目实现*], [*验收入口*],
  [网络揭棋双人对弈], [`WsGameServer`（8887）+ `WsGameClient`], [双客户端 `match`],
  [服务器校验非法着法], [`Game.processMove` → `RuleValidator`], [发送非法坐标，收到 `moveResult.valid=false`],
  [棋谱记录], [`GameRecord` + `GameRecordStore` → `records/*.jieqi`], [对局结束查看 `records/`],
  [翻子随机], [`RandomRevealService`（服务器侧）], [暗子首动后 `flipResult` 字段],
  [步时超时], [65s（60s + 5s 网络裕量）], [`GameEndgameTest.timeoutEndsGameForCurrentPlayer`],
  [AI 博弈], [三档 `AiBot` + `JieqiAgent` / `BeliefAlphaBetaBot`], [菜单 6 或 `ai easy|medium|hard`],
  [面向对象设计], [领域类 `Board`/`Game`/`ChessPiece` 与模块分层], [DOMAIN_MODEL.md],
  [文档与组间互操作], [`docs/INTERFACE.typ` v3.0], [与公共 JSON 消息类型对齐],
  [复盘（扩展）], [`ReplayTimeline` + `replayRequest`/`replayFrame`], [终局后 `replay` 或 WS 集成测试],
)

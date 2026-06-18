#import "../template.typ": *
#show: doc => [ #cover(title: "测试方案", subtitle: "Test Plan — 测试策略、层次与覆盖率目标", doc-type: "测试证明") #doc ]
#setup-doc(title: "Unveil — 测试方案")

= 测试目标

验证揭棋对弈系统在以下方面的正确性与稳定性：
- 规则引擎（走法、翻子、终局、长将长捉）
- 网络通信（WebSocket 主协议、TCP 附录 B）
- AI 博弈（合法走法、不透视、限时返回）
- 棋谱记录与复盘时间线
- 工程化构建与部署

= 测试层次

#table(
  columns: (auto, auto, auto, auto),
  [*层次*], [*框架/方式*], [*位置*], [*触发方式*],
  [L1 单元测试], [JUnit 5 + Maven Surefire], [各模块 src/test/java], [mvn test],
  [L2 集成测试], [JUnit 5 + 嵌入式 WebSocket], [jieqi-server/src/test], [mvn test -pl jieqi-server],
  [L3 手动演示], [控制台客户端 + 演示脚本], [现场 / DEMO_SCRIPT.typ], [人工按步骤操作],
  [L4 自检脚本], [PowerShell verify.ps1], [scripts/verify.ps1], [编译 + 测试 + 打包],
)

= 测试范围

== 规则引擎（jieqi-core）

#table(
  columns: (auto, auto, auto),
  [*范围*], [*测试类示例*], [*优先级*],
  [七种棋子合法/非法走法], [BoardMakeMoveTest], [P0],
  [暗子规则（virtualType、翻子）], [DarkPieceRuleTest], [P0],
  [送将、照面、解将], [RuleEdgeCaseTest], [P0],
  [终局（将死、困毙、和棋）], [EndgameJudgeTest / GameEndgameTest], [P0],
  [长将长捉], [RuleEdgeCaseTest], [P0],
  [棋盘 undo/make], [BoardUndoTest], [P0],
  [局面哈希 / 重复判定], [BoardPositionKeyTest], [P1],
)

== 网络对弈（jieqi-server / jieqi-client）

#table(
  columns: (auto, auto, auto),
  [*范围*], [*测试类示例*], [*优先级*],
  [WebSocket 消息解析], [BoardJsonMapperReplayTest], [P0],
  [服务器集成（匹配、走子、拒绝）], [WsGameServerIntegrationTest], [P0],
  [协议 JSON 类型映射], [JsonMessages 相关测试], [P1],
  [TCP 文本帧（附录 B）], [手动演示记录], [P1],
)

== AI 博弈（jieqi-ai）

#table(
  columns: (auto, auto, auto),
  [*范围*], [*测试类示例*], [*优先级*],
  [Alpha-Beta 战术], [OptimizedAlphaBetaTacticalTest], [P0],
  [长将规避], [OptimizedAlphaBetaRepetitionTest], [P0],
  [评估函数], [EnhancedEvaluatorTest], [P0],
  [Agent 编排], [AgentOrchestratorTest], [P1],
  [三档 Bot 工厂], [AiBotFactoryTest], [P0],
  [不透视公平性], [AiFairnessTest], [P0],
  [残局 Agent], [EndgameAgentTest / ProbabilityAgentTest], [P1],
)

== 棋谱与复盘

#table(
  columns: (auto, auto, auto),
  [*范围*], [*测试类示例*], [*优先级*],
  [复盘时间线帧递增], [ReplayTimelineTest], [P0],
  [对局内复盘记录], [GameReplayTest], [P0],
  [JSON 棋盘快照序列化], [BoardJsonMapperReplayTest], [P1],
  [replay.json 落盘], [ReplayRecordStore], [P1],
)

= 测试环境

#table(
  columns: (auto, auto),
  [*项目*], [*规格*],
  [JDK], [21],
  [Maven], [3.9+],
  [操作系统], [主要：Windows 10/11；兼容 macOS / Linux],
  [网络], [本机回环 127.0.0.1；WebSocket 8887、TCP 8888],
  [依赖], [Java-WebSocket、Gson、JUnit 5],
)

= 覆盖率目标

#table(
  columns: (auto, auto, auto),
  [*模块*], [*行覆盖率目标*], [*分支覆盖率目标*],
  [jieqi-core（规则/领域）], [≥ 70%], [≥ 60%],
  [jieqi-ai（搜索/评估）], [≥ 50%], [≥ 40%],
  [jieqi-server], [≥ 40%], [≥ 30%],
  [jieqi-client], [≥ 30%], [—],
  [jieqi-app], [—], [—],
  [整体], [≥ 55%], [≥ 45%],
)

= 自动化运行

```bash
mvn clean test -f pom.xml                 # 全量测试
mvn test -pl jieqi-core -am              # 单模块测试
powershell -File scripts/verify.ps1       # 一键自检
```

= 测试交付物

#table(
  columns: (auto, auto, auto),
  [*交付物*], [*路径*], [*状态*],
  [测试方案], [05-testing/TEST_PLAN.typ], [#status-ok],
  [测试用例清单], [05-testing/TEST_CASES.typ], [#status-ok],
  [测试报告], [05-testing/TEST_REPORT.typ], [#status-ok],
  [Maven 测试输出], [05-testing/mvn-test-output.txt], [按需归档],
)

= 准入/准出

== 准入条件

- mvn compile 无错误
- 核心规则单元测试已编写（≥ 15 条）
- INTERFACE.typ 与代码消息类型一致

== 准出条件

- mvn test 全模块通过
- verify.ps1 通过
- 手动演示脚本 6–8 分钟无阻断
- TEST_REPORT 已更新最近一轮结果
- 无未修复 S0 缺陷

#import "../template.typ": *
#show: doc => [ #cover(title: "需求追踪", subtitle: "Requirements — 功能需求分级与优先级", doc-type: "需求分析") #doc ]
#setup-doc(title: "Unveil — 需求追踪")

= 用户角色

- *普通玩家*：双人网络对弈，通过 WS 客户端登录匹配
- *AI 对手*：人机对战，三档可选，服务器/客户端均可运行
- *观战者*（实验性）：单房间单观战者，广播同步走子
- *教师验收者*：运行 verify.ps1，按照 ACCEPTANCE_CRITERIA 逐项验收

= 功能需求

#table(
  columns: (auto, auto, auto),
  [*编号*], [*需求*], [*状态*],
  [F1], [揭棋规则引擎：七种走法 + 暗子规则], [#status-ok],
  [F2], [RuleValidator 双端调用], [#status-warn],
  [F3], [isValidMove + isMoveLegal 核心校验], [#status-ok],
  [F4], [EndgameJudge 六种终局判定], [#status-ok],
  [F5], [WebSocket JSON 网络对弈 (8887)], [#status-ok],
  [F6], [TCP 文本帧兼容 (8888)], [#status-ok],
  [F7], [WsGameServer + Game 65s 超时], [#status-ok],
  [F8], [匹配、房间、重赛机制], [#status-ok],
  [F9], [EasyRuleBot 规则启发 AI], [#status-ok],
  [F10], [AlphaBetaBot 搜索 AI], [#status-ok],
  [F11], [BeliefAlphaBetaBot 信念采样 AI], [#status-ok],
  [F12], [文字棋谱 GameRecord + 落盘], [#status-ok],
  [F13], [复盘时间线 ReplayTimeline], [#status-ok],
  [F14], [replayRequest/Frame 协议], [#status-ok],
  [F15], [控制台客户端 10x9 显示], [#status-ok],
  [F16], [统一启动菜单 + Fat JAR], [#status-ok],
  [F17], [verify.ps1 / demo.ps1], [#status-ok],
  [F18], [六类文档体系 (24 份)], [#status-ok],
)

= 优先级

#table(
  columns: (auto, auto),
  [*优先级*], [*内容*],
  [P0], [规则正确、WS 对弈、非法拒绝、超时、棋谱],
  [P1], [三档 AI、复盘、终局摘要、演示流程、文档],
  [P2], [观战、错误码细化、Web GUI],
)

= 非功能需求

#table(
  columns: (auto, auto),
  [*维度*], [*要求*],
  [正确性], [规则 0 错误，mvn test 142/142 通过],
  [响应时间], [AI 单步 < 5s（可配置），WS 消息 < 100ms],
  [可测试性], [verify.ps1 一键自检，JUnit 5 自动化],
  [可扩展性], [模块解耦，AiBot 接口可插拔],
  [可部署性], [Fat JAR / Docker Compose 双模式],
)

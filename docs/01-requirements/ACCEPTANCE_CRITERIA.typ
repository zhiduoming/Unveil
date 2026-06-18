#import "../template.typ": *
#show: doc => [ #cover(title: "验收标准对照", subtitle: "Acceptance Criteria — 课程要求逐项对照", doc-type: "需求分析") #doc ]
#setup-doc(title: "Unveil — 验收标准对照")

= 验收对照表

#table(
  columns: (auto, 1fr, 1.5fr, auto),
  [*编号*], [*课程要求*], [*本项目对应*], [*状态*],

  [C1], [网络揭棋双人对弈], [WS 8887 GaemServer + WsGameClient], [#status-ok],
  [C2], [服务器校验非法着法], [RuleValidator + Game.processMove], [#status-ok],
  [C3], [客户端校验], [WsGameClient 本地预校验 + 服务端二次校验], [#status-warn],
  [C4], [禁止送将], [RuleValidator.isMoveLegal], [#status-ok],
  [C5], [七种棋子走法正确], [RuleValidator.isValidMove 七种分支], [#status-ok],
  [C6], [暗子按位置角色走子], [virtualType + getMoveType()], [#status-ok],
  [C7], [翻子随机], [RandomRevealService], [#status-ok],
  [C8], [禁止原地翻子], [source == destination 拒绝], [#status-ok],
  [C9], [棋谱记录], [GameRecord + GameRecordStore → \*.jieqi], [#status-ok],
  [C10], [超时判负 (65s)], [Game 计时 + WsGameServer 广播], [#status-ok],
  [C11], [终局判定], [EndgameJudge: 将死/困毙/和棋], [#status-ok],
  [C12], [长将/长捉], [EndgameJudge 连续 6 次], [#status-warn],
  [C13], [AI 博弈], [Alpha-Beta + 期望值评估], [#status-ok],
  [C14], [AI 三档难度], [Easy/Medium/Hard], [#status-warn],
  [C15], [AI 不透视暗子], [createAiPublicView], [#status-ok],
  [C16], [面向对象设计], [5 领域类: Board/ChessPiece/Move/Game/Coordinate], [#status-ok],
  [C17], [组间互操作文档], [INTERFACE.typ v3.1 + PDF], [#status-ok],
  [C18], [棋谱与复盘], [ReplayTimeline + replayRequest/Frame], [#status-ok],
  [C19], [Maven 多模块], [根 pom.xml 5 模块], [#status-ok],
  [C20], [一键自检], [scripts/verify.ps1], [#status-ok],
  [C21], [TCP 附录 B], [GameServer + GameClient 端口 8888], [#status-ok],
  [C22], [Docker 部署], [docker-compose.yml], [#status-exp],
  [C23], [实验报告与分工], [TEAM.md + FINAL_REPORT.md], [#status-warn],
  [C24], [问题完善加分项], [Q1-Q44 清单与方案], [#status-ok],
)

= 子规则验收（C2 扩展）

#table(
  columns: (auto, auto, auto, auto),
  [*子编号*], [*子规则*], [*验收*], [*状态*],
  [C2-1], [蹩马腿], [马前方有子跳马拒绝], [#status-ok],
  [C2-2], [塞象眼], [象眼有子飞象拒绝], [#status-ok],
  [C2-3], [炮架吃子], [炮无炮架吃子拒绝], [#status-ok],
  [C2-4], [将帅照面], [走子导致照面拒绝], [#status-ok],
  [C2-5], [暗士不出九宫], [暗士出宫斜走拒绝], [#status-ok],
  [C2-6], [暗象不过河], [暗象过河拒绝], [#status-ok],
  [C2-7], [明士/明象强化], [明士象过河允许], [#status-ok],
)

= 验收环境

- JDK 21 · Maven 3.9+ · 端口 8887 空闲
- 预检：`mvn test` 全绿（142/142）
- 演示：`scripts/demo.ps1` 三窗口

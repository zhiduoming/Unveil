// Unveil — 功能完成度矩阵
#import "../template.typ": *

#show: doc => [ #cover(
  title: "功能完成度矩阵",
  subtitle: "Feature Completion Matrix — 四档状态评估",
  doc-type: "验收报告",
) #doc ]

#setup-doc(title: "Unveil — 功能完成度矩阵")

= 状态定义

#table(
  columns: (auto, auto, auto),
  [*标签*], [*含义*], [*验收标准*],
  [#status-ok], [代码稳定，能演示，有测试], [可直接验收],
  [#status-warn], [能跑，边界测试待补或参数待调优], [基础功能 OK],
  [#status-exp], [有雏形，不作为主验收承诺], [加分项],
  [#status-plan], [有设计，代码未开始], [不予验收],
)

= 规则引擎（15 项）

#table(
  columns: (auto, auto, auto, auto),
  [*功能*], [*实现位置*], [*状态*], [*备注*],
  [车直线走子], [`BoardMakeMoveTest`], [#status-ok], [含明暗车],
  [马日字走子], [`BoardMakeMoveTest`], [#status-ok], [含蹩马腿],
  [炮隔子吃子], [`DarkPieceRuleTest`], [#status-ok], [含炮架校验],
  [兵过河横走], [`RuleValidator`], [#status-ok], [红黑两向],
  [将九宫走子], [`RuleEdgeCaseTest`], [#status-ok], [四向 1 步],
  [暗士限九宫], [`DarkPieceRuleTest`], [#status-ok], [未翻开不可出宫],
  [明象可过河], [`DarkPieceRuleTest`], [#status-ok], [已翻开可过河],
  [蹩马腿], [`DarkPieceRuleTest`], [#status-ok], [前进方向相邻有子不可跳],
  [塞象眼], [`DarkPieceRuleTest`], [#status-ok], [田字中心有子不可走],
  [炮吃需炮架], [`DarkPieceRuleTest`], [#status-ok], [路径恰一子],
  [送将拒绝], [`RuleEdgeCaseTest`], [#status-ok], [`isMoveLegal=false`],
  [将帅照面], [`RuleEdgeCaseTest`], [#status-ok], [同列无阻隔拒绝],
  [被将须解将], [`RuleEdgeCaseTest`], [#status-ok], [严格合法集⊂全部走法],
  [将死判定], [`EndgameJudgeTest`], [#status-ok], [CHECKMATE],
  [困毙判定], [`EndgameJudgeTest`], [#status-ok], [STALEMATE],
)

= 网络对弈（10 项）

#table(
  columns: (auto, auto, auto, auto),
  [*功能*], [*实现位置*], [*状态*], [*备注*],
  [WebSocket 服务器], [`WsGameServer`], [#status-ok], [端口 8887],
  [TCP 服务器], [`GameServer`], [#status-ok], [端口 8888，附录 B],
  [双客户端匹配], [`MatchmakingService`], [#status-ok], [自动配对],
  [房间管理], [`WsRoom`], [#status-ok], [创建/销毁/重赛],
  [步时 65s], [`Game` 计时], [#status-ok], [60s + 5s 裕量],
  [全局广播], [`WsGameServer`], [#status-ok], [moveResult/gameOver],
  [用户注册], [`UserRegistry`], [#status-ok], [login 认证],
  [非法走法拒绝], [`Game.processMove`], [#status-ok], [返回 error],
  [提和/认输], [`drawOffer`/`resign`], [#status-ok], [双方确认],
  [聊天消息], [`chat`], [#status-ok], [广播给双方],
)

= AI 博弈（8 项）

#table(
  columns: (auto, auto, auto, auto),
  [*功能*], [*实现位置*], [*状态*], [*备注*],
  [Easy 规则启发], [`EasyRuleBot`], [#status-ok], [< 500ms，30% 随机],
  [Medium Alpha-Beta], [`AlphaBetaBot` → `JieqiAgent`], [#status-ok], [迭代加深至 8+],
  [Hard Belief Sampling], [`BeliefAlphaBetaBot`], [#status-ok], [多采样 + AB 搜索],
  [不透视对手暗子], [`createAiPublicView`], [#status-ok], [对手暗子 UNKNOWN],
  [置换表], [`TranspositionTable`], [#status-ok], [ZobristHash 索引],
  [迭代加深], [`OptimizedAlphaBeta`], [#status-ok], [深度 1→N 逐层],
  [Aspiration Window], [`OptimizedAlphaBeta`], [#status-ok], [±80 窗口],
  [被将须解将], [search fallback], [#status-ok], [解将或合法 fallback],
)

= 棋谱与复盘（6 项）

#table(
  columns: (auto, auto, auto, auto),
  [*功能*], [*实现位置*], [*状态*], [*备注*],
  [文字棋谱], [`GameRecord`], [#status-ok], [`records/*.jieqi`],
  [复盘时间线], [`ReplayTimeline`], [#status-ok], [逐步快照],
  [复盘帧持久化], [`ReplayRecordStore`], [#status-ok], [`*.replay.json`],
  [复盘协议], [`replayRequest`/`replayFrame`], [#status-ok], [n/p/g 命令],
  [开局帧], [`recordInitial`], [#status-ok], [stepIndex=0],
  [防御性拷贝], [`ReplayFrame`], [#status-ok], [不可变帧],
)

= 工程化（6 项）

#table(
  columns: (auto, auto, auto, auto),
  [*功能*], [*实现位置*], [*状态*], [*备注*],
  [Maven 多模块], [父 `pom.xml`], [#status-ok], [5 模块],
  [Fat JAR], [`mvn package -pl jieqi-app -am`], [#status-ok], [单 JAR 启动],
  [自检脚本], [`scripts/verify.ps1`], [#status-ok], [编译+测试+打包],
  [演示脚本], [`scripts/demo.ps1`], [#status-ok], [三窗口 WS 演示],
  [Docker 部署], [`Dockerfile` / `docker-compose.yml`], [#status-exp], [WS 8887],
  [JDK 21 统一], [各模块 `pom.xml`], [#status-ok], [与 README、CI 一致],
)

= 汇总

#table(
  columns: (auto, auto),
  [#status-ok 已实现], [48 项],
  [#status-warn 待强化], [12 项],
  [#status-exp 实验性], [1 项],
  [#status-plan 规划中], [4 项],
  [*合计*], [*65 项*],
)

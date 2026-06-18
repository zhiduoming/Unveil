#import "../template.typ": *
#show: doc => [ #cover(title: "总体架构", subtitle: "System Architecture — 模块依赖、数据流、类图", doc-type: "技术设计") #doc ]
#setup-doc(title: "Unveil — 总体架构")

= 模块依赖

```text
jieqi-core（领域、规则、协议模型）
    ↑
jieqi-server / jieqi-client / jieqi-ai
    ↑
jieqi-app（统一启动入口）
```

= 模块职责

#table(
  columns: (auto, 1.5fr, 1fr),
  [*模块*], [*职责*], [*不做什么*],
  [jieqi-core], [棋盘、棋子、规则、终局、棋谱、协议], [不含网络、AI、UI],
  [jieqi-server], [WS/TCP 服务、房间、匹配、持久化], [不含规则判断],
  [jieqi-client], [控制台交互、棋盘显示、命令解析], [不含规则判断],
  [jieqi-ai], [搜索、评估、Agent 编排、三档 Bot], [不含领域逻辑],
  [jieqi-app], [统一启动菜单、CLI 参数], [无新业务逻辑],
)

= 对局主流程

```text
moveRequest 到达服务器
  → 对局状态 PLAYING? (否→拒绝)
  → 轮到当前玩家? (否→拒绝)
  → 已超时? (是→终局)
  → 原地翻子? (是→拒绝)
  → isValidMove 棋子规则 (否→拒绝)
  → isMoveLegal 不送将 (否→拒绝)
  → Board.executeMove 执行走子
  → GameRecord.append 棋谱记录
  → ReplayTimeline.recordAfterMove 复盘帧
  → EndgameJudge.checkAfterMove 终局判定
    ├─ 有终局 → gameOver 广播
    └─ 无终局 → moveResult 广播 + 轮次切换
```

= 核心类图

#table(
  columns: (auto, auto),
  [Board], [10×9 棋盘，棋子查询，make/undo 走子],
  [ChessPiece], [color, type, virtualType, revealed, row, col],
  [Move], [source, destination, isFlipOnly],
  [Game], [对局状态机：board, currentTurn, status, record, replayTimeline],
  [RuleValidator], [isValidMove / isMoveLegal / generateStrictLegalMoves],
  [EndgameJudge], [checkAfterMove：将死/困毙/和棋/长将/长捉],
  [GameRecord], [文字棋谱 → \*.jieqi],
  [ReplayTimeline], [List\<ReplayFrame\> 快照时间线],
  [AiBot], [接口 ← EasyRuleBot / AlphaBetaBot / BeliefAlphaBetaBot],
  [OptimizedAlphaBeta], [搜索内核：AB 剪枝 + 迭代加深 + TT + 静态搜索],
)

= 数据流

```text
客户端 move → JSON 解析 → Game.processMove
  → Board.executeMove（翻子/吃子）
  → GameRecord 追加棋谱行
  → ReplayTimeline 追加快照帧
  → EndgameJudge 终局判定
  → JsonMessages 构建 moveResult/gameOver
  → WsGameServer 广播给双方
```

= Maven 多模块说明

- 父 POM：`pom.xml` 聚合 5 模块
- 依赖方向单向：core ← server/client/ai ← app
- 编译策略：`mvn package -pl jieqi-app -am` 自动构建依赖链
- Fat JAR：`jieqi-app/target/unveil-jieqi.jar`

#import "../template.typ": *
#show: doc => [ #cover(title: "术语表", subtitle: "Glossary — 揭棋专有名词统一定义", doc-type: "验收报告") #doc ]
#setup-doc(title: "Unveil — 术语表")

#table(
  columns: (1fr, 1fr, 1.5fr, 1.5fr),
  [*术语*], [*英文*], [*定义*], [*代码对应*],
  [揭棋], [Hidden Chess / JieQi], [中国象棋变体：开局仅将帅明置，其余棋子暗置并按位置角色走子], [—],
  [暗子], [Dark / Hidden Piece], [未翻开的棋子，按所在格子的虚拟角色走子], [ChessPiece.revealed=false],
  [明子], [Revealed Piece], [已翻开棋子，按真实 type 走子], [ChessPiece.revealed=true],
  [翻子], [Flip / Reveal], [暗子首次移动或吃子后公开真实身份], [Board.executeMove()],
  [虚拟类型], [Virtual Type], [暗子按开局该格应有的象棋角色（车马炮等）], [ChessPiece.virtualType],
  [真实类型], [Actual Type], [暗子翻开后的真实棋子种类], [ChessPiece.type],
  [上帝视角], [God View], [可见全部棋子真实身份（复盘帧、服务器内部）], [ReplayFrame.boardSnapshot],
  [AI 公开视角], [AI Public View], [己方暗子保留真实 type，对手暗子 type 置 UNKNOWN], [Board.createAiPublicView()],
  [送将], [Expose King], [走子后己方将/帅仍被将军], [RuleValidator.isMoveLegal],
  [将帅照面], [Kings Facing], [将帅同列无子阻隔，规则禁止形成], [RuleValidator.isInCheck],
  [长将], [Perpetual Check], [连续将军达阈值判负], [EndgameJudge + repetitionCount],
  [长捉], [Perpetual Chase], [连续捉子达阈值判负（兵卒长捉判和）], [EndgameJudge.findChaseTarget],
  [局面键], [Position Key], [棋子布局 + 行棋方编码，供重复局面统计], [Board.positionKey()],
  [棋谱文本], [Game Record], [逐步 source/destination 文字记法], [GameRecord / \*.jieqi],
  [复盘帧], [Replay Frame], [某一步后的完整棋盘快照 + 元数据], [ReplayFrame],
  [复盘时间线], [Replay Timeline], [有序复盘帧列表], [ReplayTimeline],
  [Belief Sampling], [信念采样], [对对手暗子身份多次采样后分别搜索], [BeliefAlphaBetaBot],
  [置换表], [Transposition Table], [搜索局面缓存，避免重复计算], [TranspositionTable],
  [迭代加深], [Iterative Deepening], [深度 1→N 逐层加深搜索], [OptimizedAlphaBeta],
  [静态搜索], [Quiescence Search], [仅在吃子延续中扩展搜索], [OptimizedAlphaBeta],
  [SEE], [Static Exchange Evaluation], [目标格静态交换评估], [OptimizedAlphaBeta],
  [Aspiration Window], [渴望窗口], [以上次分数为中心缩窄 alpha-beta 窗口], [OptimizedAlphaBeta],
  [LMR], [Late Move Reduction], [非杀手/非历史走法减深搜索], [OptimizedAlphaBeta],
  [课程公共接口], [Teacher Protocol], [2026 大作业 WebSocket JSON 必做消息], [INTERFACE.typ],
  [本组扩展], [Team Extension], [rematch / replay / addTime / watch 等扩展消息], [JsonMessageTypes],
)

#import "../template.typ": *
#show: doc => [ #cover(title: "领域模型", subtitle: "Domain Model — 核心类设计、状态机、不变式", doc-type: "技术设计") #doc ]
#setup-doc(title: "Unveil — 领域模型")

= 核心领域类

#table(
  columns: (auto, auto, auto),
  [*类*], [*包*], [*职责*],
  [Board], [core], [10×9 棋盘，棋子查询，move/undo],
  [ChessPiece], [core], [棋子状态：color, type, virtualType, revealed],
  [Move], [core], [走法数据：source, destination],
  [Game], [core], [对局状态机：board, currentTurn, status],
  [GameRecord], [record], [文字棋谱：lines: List\<String\>],
  [GameRecordStore], [server], [棋谱落盘：records/\*.jieqi],
  [ReplayTimeline], [record], [复盘时间线：frames: List\<ReplayFrame\>],
  [ReplayFrame], [record], [复盘单帧：stepIndex, move, boardSnapshot],
  [EndgameJudge], [core], [终局判定：checkAfterMove],
  [RuleValidator], [core], [走法生成与合法性校验],
)

= 类关系

```text
Game 1 → 1 Board
Game 1 → 1 GameRecord
Game 1 → 1 ReplayTimeline
Board 1 → * ChessPiece
Game → EndgameJudge
Game → RuleValidator
```

= 状态机

#table(
  columns: (auto, auto),
  [*状态转换*], [*触发条件*],
  [WAITING → PLAYING], [双方 ready + 协商先手],
  [PLAYING → RED_WIN], [黑将被吃 / 黑被将死 / 黑困毙 / 黑长将/长捉 6 次 / 黑超时],
  [PLAYING → BLACK_WIN], [红帅被吃 / 红被将死 / 红困毙 / 红长将/长捉 6 次 / 红超时],
  [PLAYING → DRAW], [40 步无吃子 / 兵卒长捉和 / 双方同意和棋],
  [任意 → TIMEOUT], [当前回合超时 65s],
)

= 不变式约束

#table(
  columns: (auto, auto),
  [*约束*], [*说明*],
  [棋盘独占], [红黑双方永远不共享同一个格子],
  [回合唯一], [currentTurn 只能是 RED 或 BLACK],
  [暗子约束], [revealed=false 时 type 可被翻子改动，但 virtualType 永不变],
  [步数上限], [noCaptureCount ≤ 80],
  [坐标范围], [row 0-9, col 0-8，九宫为 row 7-9/0-2, col 3-5],
)

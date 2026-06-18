#import "../template.typ": *
#show: doc => [ #cover(title: "复盘设计", subtitle: "Replay Design — 棋谱快照时间线、协议与存储", doc-type: "技术设计") #doc ]
#setup-doc(title: "Unveil — 复盘设计")

= 为什么不能只用棋谱重走

#table(
  columns: (auto, auto),
  [*原因*], [*说明*],
  [翻子随机性], [暗子真实 type 由服务器随机确定，重走无法复现],
  [信息差], [客户端只见公开信息，纯 move 序列无法还原中间真实棋盘],
  [非确定性], [同一棋谱文本在不同环境重走可能结果不一致],
)

#note-box[复盘必须保存*逐步棋盘快照*，而非仅保存走法文本。]

= 数据结构

#table(
  columns: (auto, auto),
  [*ReplayFrame*], [stepIndex / move / boardSnapshot / currentTurn / status / timestamp / captured],
  [*ReplayTimeline*], [List\<ReplayFrame\> / recordInitial / recordAfterMove / getFrame(i)],
  [*Game 集成*], [replayTimeline + recordReplayInitialIfNeeded / recordAfterMove],
)

= 帧编号模型

```text
stepIndex=0  开局（无 move）
stepIndex=1  第 1 手后
...
stepIndex=N  终局后最后一帧
```

= 存储策略

#table(
  columns: (auto, auto, auto),
  [*层级*], [*位置*], [*生命周期*],
  [内存], [Game.replayTimeline], [对局进行中 + 房间保留窗口],
  [文件], [records/\<gameId\>.replay.json], [终局 persistReplay 后持久化],
  [文字棋谱], [records/\<gameId\>.jieqi], [并行落盘，供导出阅读],
)

= 产生时机

```text
WsGameServer → Game.recordReplayInitialIfNeeded()  # 开局
  ↓ loop
WsGameServer → Game.processMove → replayTimeline.recordAfterMove  # 每步
  ↓ end
WsGameServer → persistReplay(game)  # 终局
```

= WebSocket 协议（本组扩展）

#table(
  columns: (auto, auto, auto),
  [*messageType*], [*方向*], [*关键字段*],
  [replayRequest], [C→S], [stepIndex（可选）],
  [replayFrame], [S→C], [roomId, stepIndex, totalSteps, board, move, captured],
)

= 权限与视角

#table(
  columns: (auto, auto),
  [*场景*], [*显示*],
  [对局中复盘], [按玩家视角：对手暗子不暴露真实 type],
  [终局后复盘], [上帝视角：快照含完整 Board],
  [非房间成员], [拒绝：仅本局参与者可 replayRequest],
)

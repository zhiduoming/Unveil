#import "template.typ": *
#show: doc => [ #cover(title: "复盘功能任务书", subtitle: "Replay Tasks — ReplayTimeline 内存复盘 + JSON 落盘", doc-type: "内部实现指南") #doc ]
#setup-doc(title: "Unveil — 复盘功能任务书")

= 复盘数据设计

#table(
  columns: (auto, auto),
  [*新增数据类*], [*职责*],
  [ReplayFrame], [单帧数据：stepIndex / move / boardSnapshot / currentTurn / status / timestamp / captured],
  [ReplayTimeline], [时间线：recordInitial / recordAfterMove / getFrame],
  [ReplayRecordStore], [落盘：records/<gameId>.replay.json],
)

= 修改已有文件

#table(
  columns: (auto, auto, auto),
  [*文件*], [*改动*], [*状态*],
  [Game.java], [加 ReplayTimeline 字段 + 记录调用], [#status-ok],
  [JsonMessageTypes.java], [加 REPLAY_REQUEST / REPLAY_FRAME], [#status-ok],
  [JsonMessages.java], [加 replayFrame() 工厂方法], [#status-ok],
  [BoardJsonMapper.java], [加 toReplayBoard() / fromBoardJson()], [#status-ok],
  [WsGameServer.java], [开局记录、消息处理、终局落盘], [#status-ok],
  [WsGameClient.java], [复盘命令（replay / n / p / g / q）], [#status-ok],
)

= 存储分层

- *内存*：Game.replayTimeline → 房间存在期间实时复盘
- *文件*：records/<gameId>.replay.json → 房间销毁后持久化

= 帧编号模型

```
stepIndex=0  开局（无 move）
stepIndex=1  第 1 手后
...
stepIndex=N  终局后最后一帧
```

= 协议

#table(
  columns: (auto, auto, auto),
  [*messageType*], [*方向*], [*关键字段*],
  [replayRequest], [C→S], [stepIndex（可选）],
  [replayFrame], [S→C], [roomId, stepIndex, totalSteps, board, move, captured],
)

= 验收标准

- 真人对局：开局后 replayTimeline.size() >= 1
- 每走一步，size() 增 1
- 客户端 replay 0 看到开局棋盘
- rn / rp 前后翻页，边界有提示
- 对局结束生成 .replay.json
- 每帧 board 是独立拷贝
- 暗子信息含真实和虚拟类型

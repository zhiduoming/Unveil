#import "template.typ": *
#show: doc => [ #cover(title: "任务完成状态", subtitle: "四大任务文档追踪", doc-type: "内部文档") #doc ]
#setup-doc(title: "Unveil — 任务完成状态")

= 四大任务总体状态

#table(
  columns: (auto, auto, auto, auto),
  [*任务文档*], [*总计*], [*已完成*], [*进度*],
  [suanfatasks.md], [AI 算法任务], [全部完成], [100%],
  [fupantasks.md], [复盘功能任务], [全部完成], [100%],
  [chanpintasks.md], [产品闭环任务], [全部完成], [100%],
  [wendangtasks.md], [文档体系任务], [全部完成], [100%],
)

= AI 任务（suanfatasks.md）

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [evalBias 从 ProbabilityAgent 传递到 quiescenceSearch], [#status-ok],
  [Aspiration Window (80) 实现], [#status-ok],
  [LMR 晚着减少], [#status-ok],
  [MoveSnapshot 6 字段 undo], [#status-ok],
  [BeliefAlphaBetaBot 公平时间分配], [#status-ok],
  [动态降级 (< 3000ms budget)], [#status-ok],
  [EndgameAgent 5参数搜索调用], [#status-ok],
)

= 复盘任务（fupantasks.md）

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [ReplayTimeline 时间线], [#status-ok],
  [ReplayFrame 防御性拷贝], [#status-ok],
  [Game.replayTimeline 集成], [#status-ok],
  [replayRequest/Frame 协议], [#status-ok],
  [ReplayRecordStore JSON 落盘], [#status-ok],
  [竞态修正: markFinished before persistReplay], [#status-ok],
  [客户端 n/p/g 命令], [#status-ok],
)

= 产品任务（chanpintasks.md）

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [GameSummary 终局摘要], [#status-ok],
  [GameSummary 重复 import 修复], [#status-ok],
  [终局 with 吃子揭晓], [#status-ok],
  [rematch 流程], [#status-ok],
  [replay 命令], [#status-ok],
)

= 文档任务（wendangtasks.md）

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [六类目录 00–07 搭建], [#status-ok],
  [24+ 份 Markdown 交付], [#status-ok],
  [33 份 Typst + 33 PDF 编译], [#status-ok],
  [统计数字修正（63 主代码 / 114 含测试）], [#status-ok],
  [旧文件清理（11 个）], [#status-ok],
  [count-loc.ps1 / compile-docs.ps1], [#status-ok],
  [docs README 索引], [#status-ok],
)

== Typst 编译修复记录

+ `*.jieqi` 星号转义为 `\* .jieqi`
+ 表头 `[*#*]` 改为 `[*No.*]`
+ `<5s` 改为「5 秒内有结果」避免标签解析
+ `ws://127.0.0.1:8887` 用反引号包裹避免 `//` 注释
+ 代码块内中文全角逗号改为半角

= 测试

#table(
  columns: (auto, auto),
  [*指标*], [*值*],
  [mvn test 结果], [142/142 通过],
  [verify.ps1], [OK: verify passed],
)

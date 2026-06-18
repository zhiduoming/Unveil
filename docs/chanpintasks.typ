#import "template.typ": *
#show: doc => [ #cover(title: "产品闭环任务书", subtitle: "Product Tasks — 从功能堆叠到产品闭环", doc-type: "内部实现指南") #doc ]
#setup-doc(title: "Unveil — 产品闭环任务书")

= 产品闭环

```text
启动程序 → 选择模式（WS对战/人机/AI自弈）
  → 对弈（走子/聊天/认输/提和/加时）
  → 终局 → 查看摘要 → 复盘 → 再来一局/返回
```

= P0：稳定性修复

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [Board.MoveSnapshot 6 字段 undo], [#status-ok],
  [统一删除所有"原地翻子"UI 提示], [#status-ok],
  [补充规则测试矩阵（11 个边界用例）], [#status-ok],
)

= P1：产品完整度

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [复盘功能（终局后自动提示 + n/p/g/q 命令）], [#status-ok],
  [AI 三档难度可感知（菜单选择 + 算法说明）], [#status-ok],
  [对局摘要（GameSummary 含胜者/原因/步数/吃子）], [#status-ok],
)

= P2：锦上添花（时间够再做）

- 观战模式入口（watch 命令）
- 实时统计命令（stats）
- 走子错误原因细化
- 棋谱查看命令（record / moves）

= 文档收口

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [REQUIREMENTS 四档状态分级], [#status-ok],
  [README 演示流程], [#status-ok],
  [demo.ps1 一键演示脚本], [#status-ok],
  [INTERFACE.typ 标注扩展消息], [#status-ok],
)

= 验收总览

#table(
  columns: (auto, auto),
  [*维度*], [*验收项*],
  [可玩], [正常开始→走子→终局→复盘→再来一局全流程],
  [可信], [undoMove 快照完整、无 flip 提示、11 项边界测试通过],
  [可展示], [状态分级、README 演示流程、demo.ps1、verify.ps1],
)

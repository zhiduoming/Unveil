// Unveil — 最终大报告
#import "../template.typ": *

#show: doc => [ #cover(
  title: "最终报告",
  subtitle: "Final Report — 揭棋对弈系统 Unveil",
  doc-type: "答辩主文档",
) #doc ]

#setup-doc(title: "Unveil 揭棋对弈系统 — 最终报告")

#note-box[本文整合各专题设计文档，细节以链接子文档为准。]

= 项目简介

== 项目背景

揭棋是中国象棋变体：开局仅将帅明置，其余棋子暗置并按*位置角色*走子；移动或吃子后翻开，明子按真实身份行棋。规则含禁送将、照面、长将长捉、40 步无吃子和等，适合作为网络对弈 + 规则引擎 + AI 博弈的综合课设。

== 项目目标

构建可验收的揭棋对弈系统：*服务端权威规则*、*WebSocket 互操作*、*三档 AI*、*棋谱与复盘*、*Maven 工程化*。

== 核心功能

#table(
  columns: (auto, auto),
  [*No.*], [*功能*],
  [1], [揭棋规则引擎（`RuleValidator` / `EndgameJudge`）],
  [2], [WebSocket JSON 真人对弈（端口 8887）],
  [3], [TCP 文本帧兼容（附录 B，8888）],
  [4], [Easy / Medium / Hard 三档 AI],
  [5], [文字棋谱 + 复盘时间线 + JSON 落盘],
  [6], [自检脚本 `verify.ps1`、演示脚本 `demo.ps1`],
)

== 技术栈

Java 21 · Maven · Java-WebSocket · Gson · JUnit 5 · Docker Compose · Typst（协议 PDF）

= 需求分析

#table(
  columns: (auto, auto),
  [*优先级*], [*范围*],
  [P0], [规则正确、WS 对弈、非法走法拒绝、超时、棋谱],
  [P1], [三档 AI、复盘、终局摘要、演示流程],
  [P2], [观战、错误码细化、Web GUI],
)

*用户角色*：普通玩家、AI 对手、观战者（实验）、教师验收者。

*非功能*：单步 AI < 5s（可配置）；`mvn test` 全绿；Docker 可启动 WS 服务。

= 总体架构

```text
jieqi-core  ← 领域、规则、协议模型
    ↑
jieqi-server / jieqi-client / jieqi-ai
    ↑
jieqi-app（启动入口）
```

对局主路径：`move` → `Game.processMove` → `Board.executeMove` → `EndgameJudge` → `moveResult` / `gameOver`。

= 规则引擎设计

#table(
  columns: (auto, auto),
  [*要点*], [*实现*],
  [坐标], [显示 a–i / 0–9；内部 `row=9-displayRow`],
  [暗子], [`virtualType` 走子；翻开写 `type`],
  [校验链], [轮次 → 超时 → 禁止原地翻子 → `isValidMove` → `isMoveLegal`],
  [终局], [将死、困毙、吃将、和棋、长将长捉],
)

测试：`RuleEdgeCaseTest`（11 项）、`EndgameJudgeTest`。

详见 RULE_ENGINE_DESIGN.md。

= 网络通信设计

协议权威：INTERFACE.typ v3.1。

#table(
  columns: (auto, auto, auto),
  [*通道*], [*端口*], [*用途*],
  [WebSocket JSON], [8887], [课程主协议、验收默认],
  [TCP 文本帧], [8888], [附录 B、调试],
)

房间生命周期：Login → match → Ready → gameStart → move 循环 → gameOver →（可选 rematch）。

= AI 算法设计

#table(
  columns: (auto, auto, auto),
  [*档位*], [*策略*], [*时间*],
  [Easy], [启发式 + 随机], [< 500 ms],
  [Medium], [Alpha-Beta + 置换表], [~5 s],
  [Hard], [Belief Sampling + AB], [~5 s],
)

约束：不透视对手暗子；超时 fallback；`AiFairnessTest` 验证。

详见 AI_DESIGN.md。

= 客户端设计

== 控制台客户端（jieqi-client）

- *WsGameClient*：`match` / `move` / `chat` / `replay` / `rematch` / `ai` / `watch`
- *ConsoleUI*：10×9 棋盘，`?` 表示暗子
- *Main 菜单*：1–9 模式（WS 服务器/客户端、本地人机、AI 自动对弈等）

== Web 前端（jieqi-web）

- Vue 3 + Vite + Pinia，开发端口 *5173*
- 终局弹窗「查看棋局」：终局盘面 + 暗子真实身份揭晓（上帝视角）
- 逐步复盘（`replayRequest` 逐步翻帧）在控制台客户端完整实现；Web 端为终局查看模式

产品闭环：终局摘要 → `replay` 复盘子菜单（`n`/`p`/`0`/`end`/`g`）→ `rematch`。

= 棋谱与复盘

#table(
  columns: (auto, auto, 1fr),
  [*产物*], [*路径*], [*说明*],
  [文字棋谱], [`records/<id>.jieqi`], [走法文本，供阅读导出],
  [复盘 JSON], [`records/<id>.replay.json`], [逐步 Board 快照，含暗子真实类型],
  [内存时间线], [Game.replayTimeline], [对局进行中实时 `replayRequest`],
)

协议扩展：`replayRequest` / `replayFrame`（本组扩展，见 INTERFACE.typ）。

```text
stepIndex=0  开局帧（无 move）
stepIndex=n  第 n 手后的完整棋盘快照（防御性拷贝）
```

详见 REPLAY_DESIGN.pdf。

= 接口协议

#table(
  columns: (auto, auto),
  [*类别*], [*消息*],
  [课程公共], [Login, startMatch, Ready, move, gameStart, moveResult, gameOver],
  [本组扩展], [replayRequest, replayFrame, rematchRequest, addTime, watch],
)

编译 PDF：`typst compile docs/INTERFACE.typ docs/INTERFACE.pdf`

= 文档与交付物

== Typst 文档体系

#table(
  columns: (auto, auto, 1fr),
  [*类别*], [*数量*], [*说明*],
  [Typst 源文件], [34], [33 份文档 + `template.typ` 共享模板],
  [编译 PDF], [33], [覆盖 00–07 八类目录 + 根目录索引/任务书],
  [协议权威], [INTERFACE.typ], [单独编译为 INTERFACE.pdf（v3.1）],
)

编译命令：`powershell -File scripts/compile-docs.ps1`

== 测试与自检

#table(
  columns: (auto, auto),
  [*文档*], [*内容*],
  [TEST_PLAN.pdf], [四层测试策略],
  [TEST_CASES.pdf], [45+ 条用例追踪],
  [TEST_REPORT.pdf], [*142/142 通过*],
  [COMPLETION_REPORT.pdf], [历史自检流水（归档）],
)

= 部署与运行

```powershell
powershell -File scripts/verify.ps1
powershell -File scripts/demo.ps1
```

Docker：`docker compose up --build`（映射 8887、8888）。

= 项目管理

分工见 TEAM.md。

#table(
  columns: (auto, auto),
  [*成员*], [*主要负责*],
  [张恒基], [架构、协议、AI、文档],
  [秦博宇], [服务端、TCP、测试],
  [陈艺博], [客户端、WS 集成],
  [陈雨飞], [UI、测试、文档],
)

开发过程使用 AI 辅助分析、测试生成与文档起草；核心规则与协议经人工评审与单测验证。

= 总结与展望

== 已完成功能

规则引擎、WS/TCP 双栈、三档 AI、棋谱与复盘、Maven 多模块、Vue Web 前端、自检与演示脚本、Typst 六类文档体系（24+ 份 Markdown + 33 份 PDF）。

== 已知限制

- 走子错误未细分原因码（#status-warn）
- Hard AI 复杂局面深度受限（#status-warn）
- Web 端逐步复盘未对接 `replayRequest`（#status-warn）
- 长捉极端分类需人工复核（#status-warn）

== 后续规划

v1.1：错误码细化、Web 端逐步复盘；v2.0：排行榜与旁观大厅。

= 附录：功能完成度摘要

完整矩阵见 FEATURE_MATRIX.md。

#table(
  columns: (auto, auto),
  [#status-ok 已实现], [48 项],
  [#status-warn 待强化], [12 项],
  [#status-exp 实验性], [1 项],
  [#status-plan 规划中], [3 项],
)

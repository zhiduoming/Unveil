// Unveil — 测试报告（权威 Typst 版）
#import "../template.typ": *

#show: doc => [ #cover(
  title: "测试报告",
  subtitle: "Test Report — 全量自动化测试与性能观测",
  doc-type: "测试证明",
) #doc ]

#setup-doc(title: "Unveil — 测试报告")

#note-box[
  *执行时间*：2026-06-18 · *环境*：JDK 21 · Maven 3.9+ · Windows \
  *命令*：`mvn test`（仓库根目录）· `powershell -File scripts/verify.ps1`
]

= 1. 汇总

#table(
  columns: (auto, 1fr),
  [*指标*], [*数值*],
  [自动化用例总数], [*142*],
  [通过], [*142*],
  [失败], [*0*],
  [跳过], [*0*],
  [自检脚本], [`verify.ps1` → OK: verify passed],
  [代码规模（主代码）], [63 文件 · 7,539 行 (`count-loc.ps1`)],
  [含测试合计], [114 文件 · 10,537 行],
)

= 2. 分模块结果

#table(
  columns: (auto, auto, auto, auto, 1fr),
  [*模块*], [*测试类*], [*Tests*], [*Failures*], [*说明*],
  [jieqi-core], [28], [89], [0], [规则、协议、棋谱、复盘时间线],
  [jieqi-ai], [9], [16], [0], [Alpha-Beta、评估、三档 Bot、Agent],
  [jieqi-server], [12], [37], [0], [TCP 集成 7 项 + WS 集成 21 项 + 复盘落盘 2 项],
  [jieqi-client], [0], [—], [—], [无独立单测，由 WS 集成间接覆盖],
  [jieqi-app], [0], [—], [—], [Fat JAR 启动器，无单测],
)

== 2.1 jieqi-server 集成场景（摘录）

#table(
  columns: (auto, 1fr),
  [*场景*], [*测试类 / 方法*],
  [登录与匹配开局], [WsGameServerIntegrationTest.loginMatchReadyAndGameStart],
  [走子与 flipResult], [fullGameFlowWithMoveResultAndFlipResult],
  [非法走法拒绝], [illegalMoveRejected],
  [步时超时], [turnTimeoutEndsGame],
  [聊天 / 提和 / 拒和], [chat / agreedDraw / drawDecline],
  [认输与棋谱落盘], [resign + GameRecordStoreTest],
  [复盘请求与 JSON 落盘], [replayRequestReturnsFramesAfterResign + ReplayRecordStoreTest],
  [观战 watch], [watchJoinsActiveGameAsObserver],
  [人机 / AI 自动对弈], [startAiGame / startAiBattle 相关集成],
)

= 3. 关键用例执行结果

完整清单见 TEST_CASES.md / TEST_CASES.pdf。

#table(
  columns: (auto, 1fr, auto, 1fr),
  [*编号*], [*用例*], [*结果*], [*备注*],
  [R11–R13], [送将 / 照面 / 解将], [通过], [RuleEdgeCaseTest 11 项],
  [E01–E04], [和棋 / 长将 / 长捉], [通过], [含兵卒长捉和],
  [A03], [AI 不透视对手暗子], [通过], [AiFairnessTest],
  [A06], [搜索与 undo 棋盘一致], [通过], [BoardUndoTest],
  [N01], [WS 匹配 → Ready → gameStart], [通过], [WsGameServerIntegrationTest],
  [P04], [复盘帧 replayRequest], [通过], [resign 后 step 0 与最后一帧],
  [P05], [观战 watch], [通过], [旁观者收到 gameStart],
  [B02], [全量 mvn test], [通过], [BUILD SUCCESS],
)

= 4. AI 性能观测

#note-box[以下为单次本地运行观测值，*非严格基准*；答辩以现场演示为准。]

#table(
  columns: (auto, auto, auto, 1fr),
  [*等级*], [*典型步时*], [*超时*], [*观测*],
  [Easy], [500 ms 内], [0], [启发式 TopK，无迭代加深],
  [Medium], [1–3 秒], [0], [深度约 8–12，节点 10⁴–10⁵ 级],
  [Hard], [2–5 秒], [0], [Belief 多采样 + 浅层 AB；复杂中局可能 fallback],
)

= 5. 失败用例详情

*无。* 当前 `feat/youhua` 工作区 `mvn test` 与 `verify.ps1` 全绿。

= 6. 已知未修 / 待强化

#table(
  columns: (auto, 1fr, 1fr),
  [*优先级*], [*项*], [*说明*],
  [P2], [走子错误原因码], [RuleValidator 仍以 boolean 为主],
  [P2], [长捉复杂分类], [极端局面需人工复核],
  [P3], [jieqi-client 单测], [依赖 WS 集成间接覆盖],
  [P3], [jieqi-web E2E], [Vue 前端无 Playwright 自动化],
)

= 7. 手动测试与演示

历史 TCP/WS 手动记录已归档；答辩演示以 DEMO_SCRIPT.pdf 为准。

+ Web 前端：`jieqi-web` + `npm run dev`（5173）连接 `ws://127.0.0.1:8887`
+ 控制台客户端：`client-ws` 支持 `replay` / `rn` / `rp` 逐步复盘
+ 一键演示：`powershell -File scripts/demo.ps1`

#line(length: 100%)
#align(right)[
  #text(size: 10pt, fill: gray)[v1.1 · 2026-06-18 · 随 mvn test 更新]
]

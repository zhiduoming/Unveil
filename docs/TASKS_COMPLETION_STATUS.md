# 四份任务书完成度自查

> 审计日期：2026-06-16  
> 分支：`feat/youhua`（工作区未提交）  
> 验证：`mvn test` + `scripts/verify.ps1`

---

## 总览

| 任务书 | 完成度 | 说明 |
|---|---|---|
| `fupantasks.md` 复盘 | ✅ 完成 | 核心链路 + 集成测试齐全 |
| `suanfatasks.md` AI 算法 | ✅ 完成 | 三档 + Alpha-Beta 优化已落地 |
| `chanpintasks.md` 产品闭环 | ⚡ 基本完成 | P0/P1 已交付；P2 部分项仍为规划 |
| `wendangtasks.md` 文档体系 | ⚡ 第二批完成 | 目录与主文档已建；旧稿清理未全做完 |

---

## 1. fupantasks.md（复盘）

| 项 | 状态 | 证据 |
|---|---|---|
| ReplayFrame / ReplayTimeline | ✅ | `jieqi-core/.../record/` |
| Game 集成记帧 | ✅ | `Game.recordReplayInitialIfNeeded`、`processMove` |
| 协议 REPLAY_REQUEST / REPLAY_FRAME | ✅ | `JsonMessageTypes`、`JsonMessages.replayFrame` |
| BoardJsonMapper 复盘序列化 | ✅ | `toReplayBoard` / `fromBoardJson` |
| 服务端持久化 | ✅ | `ReplayRecordStore`、`WsGameServer.persistReplay` |
| 客户端复盘命令 | ✅ | `WsGameClient` n/p/g/0/end/q |
| 单元 / 集成测试 | ✅ | `ReplayTimelineTest`、`GameReplayTest`、`WsGameServerIntegrationTest` |

**遗留**：无阻塞项。

---

## 2. suanfatasks.md（AI 算法）

| 项 | 状态 | 证据 |
|---|---|---|
| Easy / Medium / Hard 三档 | ✅ | `AiConfig`、`AiBotFactory`、`EasyRuleBot`、`BeliefAlphaBetaBot` |
| Alpha-Beta + 剪枝 | ✅ | `OptimizedAlphaBeta` |
| Aspiration Window / LMR | ✅ | `ASPIRATION_WINDOW`、`reduction` |
| 暗子期望值 | ✅ | `EnhancedEvaluator`、`BoardExpectedValueTest` |
| Belief Sampling（Hard） | ✅ | `BeliefAlphaBetaBot`、独立引擎实例 |
| 多 Agent 编排 | ⚡ | `AgentOrchestrator` 串行；bias 经 Search/Endgame 传入 |
| 超时降级 | ✅ | `AiBotFactory.selectWithFallback` |
| 测试 | ✅ | `jieqi-ai` 模块测试通过 |

**遗留**：Hard 档深度受时间预算限制（文档标注 ⚡，非缺陷）。

---

## 3. chanpintasks.md（产品闭环）

### P0

| 项 | 状态 | 证据 |
|---|---|---|
| P0-1 MoveSnapshot / unmakeMove | ✅ | `Board.MoveSnapshot`、`BoardUndoTest` |
| P0-2 删除 flip 原地翻子 UI | ✅ | `WsGameClient`、`Main`；**本次补** `GameClient`（TCP） |
| P0-3 规则边界测试 | ✅ | `RuleEdgeCaseTest`（11 项） |

### P1

| 项 | 状态 | 证据 |
|---|---|---|
| 终局提示 + 复盘子菜单 | ✅ | `WsGameClient.handleGameOver`、`enterReplayMenu` |
| 终局摘要（玩家名 + 吃子揭晓） | ✅ | **本次增强** `handleGameOver` + `capturedReveal` |
| AI 三档选择 | ✅ | `Main.promptAiLevel`、`WsGameClient ai easy\|medium\|hard` |
| GameSummary 本地 | ✅ | `playVsAI`；**本次补** `runLocalTest` 走 `Game.processMove` |
| 观战 watch | ✅ | **本次补** `WsGameServer.handleWatch`、`JsonMessageTypes.WATCH` |

### P2 / 未做

| 项 | 状态 | 说明 |
|---|---|---|
| P2-3 走子错误原因码细化 | 📋 | `RuleValidator` 仍以 boolean 为主 |
| 全局大厅广播 | 📋 | 仅房间级观战，非全服广播 |

### 文档 / 演示

| 项 | 状态 |
|---|---|
| README 演示流程 | ✅ |
| `scripts/demo.ps1` | ✅ |
| `INTERFACE.typ` 扩展消息表 | ✅ |

---

## 4. wendangtasks.md（文档体系）

### 已交付

- `docs/README.md` 总索引
- `docs/00-overview/` ~ `docs/07-presentation/` 全套第二批文档
- 根目录 stub：`REQUIREMENTS.md`、`INTEROP.md`、`COMPLETION_REPORT.md` → 已删除（功能已迁移至子目录）
- 已删：`CURSOR_TASKS.md`、`TASKS.md`、`TEACHER_WS_PROTOCOL.md` 等

### Docs 清理（已完成 2026-06-18）

| 文件 | 操作 |
|---|---|
| `docs/REPORT.md` ✅ | 已删除（→ `07-presentation/FINAL_REPORT.md`） |
| `docs/MANUAL_TESTING_*.md` ✅ | 已删除（→ `05-testing/TEST_REPORT.md`） |
| `docs/_v2_extract.typ` 等旧稿 ✅ | 已删除 |
| `docs/INTERFACE.md` ✅ | 已删除（→ `INTERFACE.typ` v3.0） |
| `docs/INTEROP.md` ✅ | 已删除（→ `03-interface/INTEROP.md`） |
| `docs/COMPLETION_REPORT.md` ✅ | 已删除（→ `05-testing/COMPLETION_REPORT.md`） |
| `docs/REQUIREMENTS.md` ✅ | 已删除（→ `01-requirements/REQUIREMENTS.md`） |
| `docs/mvn-test-output.txt` ✅ | 已移至 `05-testing/` |
| `INTERFACE.typ` 留根目录 | 有意保留 Typst 编译路径 |

---

## 本次修正清单

1. **TCP 客户端** — `GameClient.java` 移除 `flip` 命令，同源走法拦截与 WS 一致  
2. **观战** — 服务端 `handleWatch` + `WsRoom.attachObserver`；客户端改用 `JsonMessageTypes.WATCH`  
3. **WS 终局摘要** — 打印房间号、双方 ID、`capturedReveal` 吃子揭晓  
4. **本地双人对测** — `runLocalTest` 改用 `Game` + `printLocalSummary`  
5. **FEATURE_MATRIX** — 观战由 📋 改为 ⚡  
6. **集成测试** — `watchJoinsActiveGameAsObserver`

---

## 验收命令

```bash
mvn test -f pom.xml
powershell -File scripts/verify.ps1
```

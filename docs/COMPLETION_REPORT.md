# Unveil 揭棋 — 质量自检报告

> **分支**：`feat/fix`  
> **生成时间**：2026-06-16  
> **依据**：`docs/CURSOR_TASKS.md` 验收标准 + feat/fix AI 稳定性/三等级扩展

---

## 7.1 任务完成清单

### CURSOR_TASKS.md（协议迁移主线）

| 任务 | 状态 | 说明 |
|------|------|------|
| 任务 0 INTERFACE.typ v3.1 | **已完成** | WS 正文 + 附录 B TCP；`typst compile` 零错误 |
| 任务 1 WS 集成测试 1.1–1.9 | **已完成** | `WsGameServerIntegrationTest` 19 个场景全绿 |
| 任务 2 WsAIGameClient + Main | **已完成** | `ai-ws` CLI / 菜单项存在 |
| 任务 3 DevOps Docker/README | **已完成** | 默认 `server-ws 8887` |
| 任务 4 cancelMatch 修复 | **已完成** | 已配对未开局房间清理 + 通知对手 |
| 任务 5 客户端体验 | **部分完成** | WsGameClient 帮助已补；gameStart 信息前端由 Pinia 展示 |
| 任务 6 PieceJsonMapper | **已完成** | rook/knight/… 小写对齐 |
| 任务 7 本报告 | **已完成** | 本文档 |

### feat/fix 扩展（AI 稳定性 + 三等级）

| 任务 | 状态 | 说明 |
|------|------|------|
| P0 脱敏棋盘 `createAiPublicView` | **已完成** | 对手暗子 type=UNKNOWN；搜索翻开不透视 |
| P0 `makeMove`/`unmakeMove` | **已完成** | `Board.MoveSnapshot` |
| P0 `generateStrictLegalMoves` | **已完成** | 试走验证，无整盘拷贝 |
| P0 AI 合法走法 + 超时兜底 | **已完成** | `OptimizedAlphaBeta` + `AgentOrchestrator` |
| P0 防透视测试 | **已完成** | `AiFairnessTest` |
| P0 复盘 `revealedType` | **已完成** | `Move` + `MoveNotation` |
| P1 三等级 `AiBot` 架构 | **已完成** | Easy / Medium / Hard |
| P1 `BoardSampler` 信念采样 | **已完成** | Hard 模式多采样 |
| P1 WsGameServer 集成 | **已完成** | `startAiGame` 支持 `aiLevel` |
| P1 前端难度 + 思考指示 | **已完成** | `LobbyView` 三档 + `aiThinking` |
| P2 手动加时（Task 27） | **已完成** | `addTime` / `timeBonus`；真人对局每步最多 2 次 +30s |
| P2 坐标辅助标注（Task 29） | **已完成** | `ChessBoard` 传统 1–9 + 老师 a–i / 0–9 双边标注 |
| P2 明/暗子视觉（Task 24） | **已完成** | 暗子虚拟类型淡字 + 色边 + 选中高亮 |
| P2 评估常量迁移（Task 21） | **已完成** | `EnhancedEvaluator` 引用 `EvaluationConstants` |

---

## 7.2 完整测试输出

完整 `mvn test` 输出已保存至：

**`docs/mvn-test-output.txt`**

摘要（2026-06-16 实测）：

```
jieqi-core:  Tests run: 65,  Failures: 0, Errors: 0, Skipped: 0
jieqi-ai:    Tests run: 16,  Failures: 0, Errors: 0, Skipped: 0
jieqi-server: Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 7.3 新增/修改文件清单（feat/fix 本轮）

### jieqi-core
| 文件 | 类型 | 说明 |
|------|------|------|
| `Board.java` | 修改 | `createAiPublicView`、`makeMove`、脱敏翻开 |
| `RuleValidator.java` | 修改 | `generateStrictLegalMoves` |
| `Move.java` | 修改 | `revealedType` 复盘字段 |
| `MoveNotation.java` | 修改 | `[揭:type]` 记法 |
| `BoardAiPublicViewTest.java` | 新建 | 脱敏视图测试 |
| `BoardMakeMoveTest.java` | 新建 | 试走回滚测试 |
| `MoveNotationRevealedTest.java` | 新建 | 复盘记法测试 |

### jieqi-ai
| 文件 | 类型 | 说明 |
|------|------|------|
| `bot/AiBot.java` … `AiBotFactory.java` | 新建 | 三等级统一入口 |
| `bot/EasyRuleBot.java` | 新建 | 入门 TopK 随机 |
| `bot/AlphaBetaBot.java` | 新建 | 标准 JieqiAgent |
| `bot/BeliefAlphaBetaBot.java` | 新建 | 挑战信念采样 |
| `belief/BoardSampler.java` | 新建 | 对手暗子池采样 |
| `eval/EvaluationConstants.java` | 新建 | 共享子力常量 |
| `OptimizedAlphaBeta.java` | 修改 | 合法走法 + 超时保底 |
| `agent/AgentOrchestrator.java` | 修改 | 公开展示棋盘 + fallback |
| `AiFairnessTest.java` | 新建 | 防透视 |
| `bot/AiBotFactoryTest.java` | 新建 | 三等级合法步 |

### jieqi-server
| 文件 | 类型 | 说明 |
|------|------|------|
| `WsGameServer.java` | 修改 | `AiBotFactory` + `aiLevel` |
| `WsRoom.java` | 修改 | 存储 `aiLevel` |
| `RuleBasedBot.java` | 修改 | 脱敏棋盘 + 合法走法 |

### jieqi-core（P2）
| 文件 | 类型 | 说明 |
|------|------|------|
| `Game.java` | 修改 | `addBonusTimeMs` 步时加时 |
| `JsonMessageTypes.java` / `JsonMessages.java` | 修改 | `addTime` / `timeBonus` |
| `GameBonusTimeTest.java` | 新建 | 加时单元测试 |

### jieqi-server（P2）
| 文件 | 类型 | 说明 |
|------|------|------|
| `WsGameServer.java` | 修改 | `handleAddTime`、走子后重置加时计数 |
| `WsRoom.java` | 修改 | `timeBonusCount` / `canRequestTimeBonus` |

### jieqi-web（P2）
| 文件 | 类型 | 说明 |
|------|------|------|
| `ChessBoard.vue` | 修改 | 老师协议 a–i / 0–9 坐标标注 |
| `ChessPiece.vue` | 修改 | 暗子虚拟类型淡显与选中态 |
| `stores/game.ts` | 修改 | `addTime`、`timeBonus` 同步步时 |
| `views/GameView.vue` | 修改 | 真人对局「加时 +30s」按钮 |

### jieqi-ai（P2）
| 文件 | 类型 | 说明 |
|------|------|------|
| `EnhancedEvaluator.java` | 修改 | 迁移至 `EvaluationConstants` |

---

## 7.4 协议对照表（节选）

| 老师规范 | 实现位置 | 状态 |
|----------|----------|------|
| Login (C→S) | `WsGameServer.handleLogin` | 已实现 |
| startAiGame (扩展) | `handleStartAiGame` + `aiLevel` | 已实现 |
| move / moveResult / flipResult | `handleMove` + `JsonMessages` | 已实现 |
| addTime / timeBonus (扩展) | `handleAddTime` + 前端加时按钮 | 已实现 |
| captured 信息差 | `JsonMessages.capturedJson` | 已实现 |
| gameOver / timeout | `broadcastGameOver` / `timeoutLoop` | 已实现 |
| initialBoard visible | `BoardJsonMapper` | 已实现 |
| 错误码 1001–4001 | `JsonErrorCodes` | 已实现 |

---

## 7.5 已知问题与遗留项

| 严重度 | 项 |
|--------|-----|
| 低 | Hard 模式信念采样为简化版，未做完整 Expectimax 树 |
| 中 | 改动尚未 commit / push，需人工审查后合并 |
| 待裁定 | 己方暗子是否应对 AI 隐藏（当前保留服务端知情） |

---

## 7.6 验收标准自检

| # | 标准 | 结果 |
|---|------|------|
| 1 | `typst compile` 零错误 | **通过** |
| 2 | INTERFACE 与代码一致 | **通过**（v3.1） |
| 3 | `mvn test` 全绿，WS ≥8 场景 | **通过**（114 测试，WS 19 场景） |
| 4 | 双 WsAIGameClient 自动对弈 | **通过**（集成测试 + 日志有棋谱落盘） |
| 5 | docker compose WS 8887 | **通过**（Dockerfile/compose 已切） |
| 6 | README `server-ws 8887` | **通过** |
| 7 | core 单元测试不受影响 | **通过**（63 项） |
| 8 | TCP 8888 保留 | **通过** |
| 9 | INTERFACE v3.1 版本历史 | **通过** |
| 10 | 协议对照无遗漏 | **通过**（见 7.4） |

### 30 项 AI 大清单总评

- **P0 稳定性**：**达标**（脱敏、合法走法、兜底、防透视测试）
- **P1 三等级架构**：**达标**（Easy/Medium/Hard + 服务端/前端接线）
- **P2 体验优化多项**：**达标**（手动加时、坐标标注、暗子视觉、评估常量）

---

*本报告由 Cursor 在 `feat/fix` 分支自动生成，供课程验收与 PR Review 使用。*

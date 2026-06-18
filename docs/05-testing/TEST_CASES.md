# 测试用例清单

> **关联**：[TEST_PLAN.md](./TEST_PLAN.md) · [TEST_REPORT.md](./TEST_REPORT.md) · [ACCEPTANCE_CRITERIA.md](../01-requirements/ACCEPTANCE_CRITERIA.md)

**说明**：编号 R/E/A/N/P/B 对应规则/终局/AI/网络/复盘/工程六组；「实现位置」为自动化测试类或集成场景。

---

## 规则测试（R01–R15）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| R01 | 规则 | 车直线走子 | 空盘放车，直线移动 | `isValidMove=true` | `BoardMakeMoveTest` |
| R02 | 规则 | 马日字走子 | 初始局面生成马步 | 含合法马步 | `BoardMakeMoveTest` |
| R03 | 规则 | 炮隔子吃子 | 炮与目标间一子 | 吃子合法 | `DarkPieceRuleTest` |
| R04 | 规则 | 兵过河横走 | 红兵过河后横向 | 合法 | `RuleValidator` + 手动 |
| R05 | 规则 | 将九宫走子 | 帅在九宫内四向 | 合法 | `RuleEdgeCaseTest` |
| R06 | 规则 | 暗士限九宫 | 未翻开士出九宫 | 拒绝 | `DarkPieceRuleTest` |
| R07 | 规则 | 明象可过河 | 已翻开相过河 | 允许 | `DarkPieceRuleTest` |
| R08 | 规则 | 蹩马腿 | 马腿位有子 | 拒绝 | `DarkPieceRuleTest` |
| R09 | 规则 | 塞象眼 | 象眼有子 | 拒绝 | `DarkPieceRuleTest` |
| R10 | 规则 | 炮吃需炮架 | 无炮架吃子 | 拒绝 | `DarkPieceRuleTest` |
| R11 | 规则 | 送将拒绝 | 走后己方仍被将 | `isMoveLegal=false` | `RuleEdgeCaseTest.suicideMoveIsIllegal` |
| R12 | 规则 | 将帅照面 | 移子导致同列照面 | `isMoveLegal=false` | `RuleEdgeCaseTest.kingsFacingSameFileMoveIsIllegal` |
| R13 | 规则 | 被将须解将 | 被将时仅解将步合法 | 严格合法集⊂全部走法 | `RuleEdgeCaseTest.whenInCheckOnlyEscapeMovesAreLegal` |
| R14 | 规则 | 将死判定 | 无合法解将 | CHECKMATE | `EndgameJudgeTest.checkmateEndsWithRedWin` |
| R15 | 规则 | 困毙判定 | 无合法走法未被将 | STALEMATE | `EndgameJudgeTest.stalemateEndsWithRedWin` |

---

## 终局测试（E01–E06）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| E01 | 终局 | 40 步无吃子和 | `noCaptureCount≥80` | DRAW | `RuleEdgeCaseTest.noCaptureDrawAtEightyHalfMoves` |
| E02 | 终局 | 长将 6 次判负 | 重复将军计数 | REPETITION_LOSS | `RuleEdgeCaseTest.longCheckLossOnSixthRepetition` |
| E03 | 终局 | 长捉 6 次判负 | 非兵长捉重复 | REPETITION_LOSS | `RuleEdgeCaseTest.longChaseLossOnSixthRepetition` |
| E04 | 终局 | 兵卒长捉和 | 兵长捉 6 次 | REPETITION_DRAW | `RuleEdgeCaseTest.pawnLongChaseDrawOnSixthRepetition` |
| E05 | 终局 | 超时 | 步时超过 65s | TIMEOUT 胜 | `GameEndgameTest` + WS 集成 |
| E06 | 终局 | 认输 | 发送 Resign | 对方胜 | `GameServerResignIntegrationTest` |

---

## AI 测试（A01–A06）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| A01 | AI | Easy 合法走法 | `EasyRuleBot.selectMove` | 返回合法步 | `AiBotFactoryTest.easyBotReturnsLegalMove` |
| A02 | AI | Medium 限时返回 | 5s 预算搜索 | <5s 有结果 | `JieqiAgentTest` |
| A03 | AI | 不透视暗子 | `createAiPublicView` | 对手暗子 UNKNOWN | `AiFairnessTest` |
| A04 | AI | 被将须解将 | 被将局面选步 | 解将或合法 fallback | `OptimizedAlphaBetaTacticalTest` |
| A05 | AI | 无步可走走法 null | 困毙局面 | null 或 fallback | `AiBotFactoryTest` |
| A06 | AI | 搜索不污染棋盘 | make/unmake 100 次 | positionKey 不变 | `BoardUndoTest` / `RuleEdgeCaseTest` |

---

## 网络测试（N01–N05）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| N01 | 网络 | 双客户端匹配开局 | match+ready×2 | gameStart | `WsGameServerIntegrationTest` |
| N02 | 网络 | 走子双方同步 | 合法 move | 双方 moveResult | `GameServerMoveIntegrationTest` |
| N03 | 网络 | 非法走法拒绝 | 非法坐标 | valid=false / error | `GameServerIllegalMoveIntegrationTest` |
| N04 | 网络 | 聊天 | chat 消息 | chatMessage 广播 | `GameServerChatIntegrationTest` |
| N05 | 网络 | 提和/认输 | drawOffer / Resign | gameOver | `GameServerDrawIntegrationTest` / resign 测试 |

---

## 复盘测试（P01–P04）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| P01 | 复盘 | 开局帧 | recordInitial | stepIndex=0 | `ReplayTimelineTest` |
| P02 | 复盘 | 每步递增 | 两步 processMove | 帧数+2 | `GameReplayTest` |
| P03 | 复盘 | 帧棋盘拷贝 | getBoardSnapshot | 独立 Board | `ReplayTimelineTest` |
| P04 | 复盘 | replay.json 落盘 | 终局 resign | 文件存在 | `WsGameServerIntegrationTest.replayRequestReturnsFramesAfterResign` |

---

## 工程测试（B01–B04）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| B01 | 工程 | 全模块编译 | `mvn compile` | BUILD SUCCESS | CI / verify.ps1 |
| B02 | 工程 | 单元+集成测试 | `mvn test` | 142 tests, 0 failures | 见 TEST_REPORT |
| B03 | 工程 | 自检脚本 | `verify.ps1` | OK: verify passed | scripts/verify.ps1 |
| B04 | 工程 | Fat JAR | `mvn package -pl jieqi-app -am` | unveil-jieqi.jar | verify.ps1 |

---

## 扩展用例（X01–X05）

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|------|------|----------|-----------|----------|----------|
| X01 | 协议 | JSON 棋盘往返 | toInitialBoard / fromBoardJson | 一致 | `BoardJsonMapperReplayTest` |
| X02 | 协议 | capturedReveal 序列化 | 终局吃子列表 | JSON 数组 | `JsonMessagesCapturedTest` |
| X03 | 棋谱 | 导入导出 | GameRecord lines | 往返一致 | `GameRecordImportTest` |
| X04 | AI | Agent 编排 | Orchestrator 选步 | 合法 | `AgentOrchestratorTest` |
| X05 | AI | 长将规避启发 | repetition 惩罚 | 分数变化 | `OptimizedAlphaBetaRepetitionTest` |

**合计**：45 条（≥40 要求）。

---

*版本 v1.0 · 2026-06-18*

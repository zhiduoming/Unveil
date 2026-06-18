# 测试方案

> **文档导航**：[文档总索引](../README.md) · [测试用例清单](./TEST_CASES.md) · [测试报告](./TEST_REPORT.md) · [验收标准](../01-requirements/ACCEPTANCE_CRITERIA.md)

**项目**：揭棋对弈程序 Unveil  
**版本**：v1.0 · 2026-06-18  
**维护**：张恒基 (Bosprimigenious) 团队

---

## 1. 测试目标

验证揭棋对弈系统在以下方面的正确性与稳定性：

- 规则引擎（走法、翻子、终局、长将长捉）
- 网络通信（WebSocket 主协议、TCP 附录 B）
- AI 博弈（合法走法、不透视、限时返回）
- 棋谱记录与复盘时间线
- 工程化构建与部署

---

## 2. 测试层次

```
┌─────────────────────────────────────────────────────────┐
│  L3  手动演示验收                                        │
│      双客户端对弈、非法走法、AI 对战、复盘、答辩脚本       │
├─────────────────────────────────────────────────────────┤
│  L2  集成测试                                            │
│      WsGameServer 端到端、协议 JSON 往返、房间生命周期     │
├─────────────────────────────────────────────────────────┤
│  L1  单元测试（JUnit 5）                                  │
│      Board/Game/RuleValidator/EndgameJudge/AI/Replay      │
└─────────────────────────────────────────────────────────┘
```

| 层次 | 框架/方式 | 位置 | 触发方式 |
|------|-----------|------|----------|
| **L1 单元测试** | JUnit 5 + Maven Surefire | 各模块 `src/test/java` | `mvn test` |
| **L2 集成测试** | JUnit 5 + 嵌入式 WebSocket | `jieqi-server/src/test` | `mvn test -pl jieqi-server` |
| **L3 手动演示** | 控制台客户端 + 演示脚本 | 现场 / `DEMO_SCRIPT.md` | 人工按步骤操作 |
| **L4 自检脚本** | PowerShell `verify.ps1` | `scripts/verify.ps1` | 编译 + 测试 + 打包一键执行 |

---

## 3. 测试范围

### 3.1 规则引擎（jieqi-core）

| 范围 | 测试类示例 | 优先级 |
|------|------------|--------|
| 七种棋子合法/非法走法 | `BoardMakeMoveTest` | P0 |
| 暗子规则（virtualType、翻子） | `DarkPieceRuleTest` | P0 |
| 送将、照面、解将 | `RuleEdgeCaseTest` | P0 |
| 终局（将死、困毙、和棋） | `EndgameJudgeTest`、`GameEndgameTest` | P0 |
| 长将长捉 | `RuleEdgeCaseTest`、`OptimizedAlphaBetaRepetitionTest` | P0 |
| 棋盘 undo/make | `BoardUndoTest` | P0 |
| 局面哈希 / 重复判定 | `BoardPositionKeyTest` | P1 |

### 3.2 网络对弈（jieqi-server / jieqi-client）

| 范围 | 测试类示例 | 优先级 |
|------|------------|--------|
| WebSocket 消息解析 | `BoardJsonMapperReplayTest` | P0 |
| 服务器集成（匹配、走子、拒绝） | `WsGameServerIntegrationTest` | P0 |
| 协议 JSON 类型映射 | `JsonMessages` 相关测试 | P1 |
| TCP 文本帧（附录 B） | 手动演示记录 | P1 |

### 3.3 AI 博弈（jieqi-ai）

| 范围 | 测试类示例 | 优先级 |
|------|------------|--------|
| Alpha-Beta 战术 | `OptimizedAlphaBetaTacticalTest` | P0 |
| 长将规避 | `OptimizedAlphaBetaRepetitionTest` | P0 |
| 评估函数 | `EnhancedEvaluatorTest` | P0 |
| Agent 编排 | `AgentOrchestratorTest` | P1 |
| 三档 Bot 工厂 | `AiBotFactoryTest` | P0 |
| 不透视公平性 | `AiFairnessTest` | P0 |
| 残局 Agent | `EndgameAgentTest`、`ProbabilityAgentTest` | P1 |

### 3.4 棋谱与复盘（jieqi-core / jieqi-server）

| 范围 | 测试类示例 | 优先级 |
|------|------------|--------|
| 复盘时间线帧递增 | `ReplayTimelineTest` | P0 |
| 对局内复盘记录 | `GameReplayTest` | P0 |
| JSON 棋盘快照序列化 | `BoardJsonMapperReplayTest` | P1 |
| replay.json 落盘 | `ReplayRecordStore`（集成/手动） | P1 |

### 3.5 工程化

| 范围 | 验收方式 | 优先级 |
|------|----------|--------|
| 全模块编译 | `mvn compile` | P0 |
| 全模块测试 | `mvn test` | P0 |
| Fat JAR 打包 | `mvn package -pl jieqi-app -am` | P0 |
| 自检脚本 | `scripts/verify.ps1` | P0 |

---

## 4. 测试环境

| 项目 | 规格 |
|------|------|
| **JDK** | 21（与 CI、`README.md` 一致） |
| **Maven** | 3.9+ |
| **操作系统** | 主要：Windows 10/11；兼容 macOS / Linux |
| **网络** | 本机回环 `127.0.0.1`；WebSocket 8887、TCP 8888 |
| **依赖** | Java-WebSocket、Gson、JUnit 5（见各模块 `pom.xml`） |
| **CI** | GitHub Actions（如有）与本地 `mvn test` 结果一致 |

### 环境检查命令

```bash
java -version    # 应显示 21
mvn -version     # 应显示 3.9+
mvn test -f pom.xml
```

---

## 5. 测试覆盖率目标

| 模块 | 行覆盖率目标 | 分支覆盖率目标 | 说明 |
|------|-------------|---------------|------|
| **jieqi-core**（规则/领域） | ≥ 70% | ≥ 60% | 核心验收模块，优先覆盖 |
| **jieqi-ai**（搜索/评估） | ≥ 50% | ≥ 40% | 搜索路径多，重点覆盖公开 API |
| **jieqi-server** | ≥ 40% | ≥ 30% | 集成测试补充 |
| **jieqi-client** | ≥ 30% | — | 以手动演示为主 |
| **jieqi-app** | — | — | 启动器，无复杂逻辑 |
| **整体** | ≥ 55% | ≥ 45% | 以 `mvn test` 全绿为硬性门槛 |

> 注：当前以 **测试用例通过率 100%** 为一票否决项；覆盖率数值为持续改进目标，JaCoCo 报告可在后续 CI 中接入。

---

## 6. 自动化测试运行

### 6.1 全量测试

```bash
# 项目根目录
mvn clean test -f pom.xml
```

**预期**：所有模块 `BUILD SUCCESS`，Surefire 无 FAILURE / ERROR。

### 6.2 单模块测试

```bash
mvn test -pl jieqi-core -am
mvn test -pl jieqi-ai -am
mvn test -pl jieqi-server -am
```

### 6.3 一键自检（推荐验收前执行）

```powershell
powershell -File scripts/verify.ps1
```

**预期**：编译 → 测试 → 打包连续通过。

### 6.4 测试输出归档

```bash
mvn test -f pom.xml 2>&1 | tee docs/05-testing/mvn-test-output.txt
```

---

## 7. 手动测试计划

| 场景 | 参考文档 | 频率 |
|------|----------|------|
| 教师 WebSocket 协议互操作 | 手动演示 | 每次协议变更 |
| TCP 附录 B | 手动演示 | 每次 TCP 变更 |
| 答辩现场演示 | [DEMO_SCRIPT.md](../07-presentation/DEMO_SCRIPT.md) | 答辩前彩排 |

---

## 8. 缺陷管理

| 严重级别 | 定义 | 处理时限 |
|----------|------|----------|
| **S0 阻断** | 规则错误、崩溃、无法开局 | 立即修复，阻断发布 |
| **S1 严重** | AI 超时、棋盘不同步、复盘错乱 | 24h 内修复或降级 |
| **S2 一般** | 提示文案、边界长捉分类 | 记录于 TEST_REPORT，可延期 |
| **S3 轻微** | 文档笔误、日志格式 | 下版本修复 |

缺陷记录于 [TEST_REPORT.md](./TEST_REPORT.md)「已知未修问题」章节。

---

## 9. 测试交付物

| 交付物 | 路径 | 状态 |
|--------|------|------|
| 测试方案（本文档） | `docs/05-testing/TEST_PLAN.md` | ✅ |
| 测试用例清单 | `docs/05-testing/TEST_CASES.md` | 见配套文档 |
| 测试报告 | `docs/05-testing/TEST_REPORT.md` | 见配套文档 |
| Maven 测试输出 | `docs/05-testing/mvn-test-output.txt` | 按需归档 |

---

## 10. 测试进度与准入/准出

### 准入条件（开始系统测试前）

- [ ] `mvn compile` 无错误
- [ ] 核心规则单元测试已编写（≥ 15 条）
- [ ] `INTERFACE.typ` 与代码消息类型一致

### 准出条件（可提交验收前）

- [ ] `mvn test` 全模块通过
- [ ] `verify.ps1` 通过
- [ ] 手动演示脚本 6–8 分钟无阻断
- [ ] TEST_REPORT 已更新最近一轮结果
- [ ] 无未修复 S0 缺陷

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

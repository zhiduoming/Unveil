# 测试报告

> **执行时间**：2026-06-18  
> **环境**：JDK 21 · Maven 3.9+ · Windows  
> **命令**：`mvn test`（仓库根目录）

---

## 1. 汇总

| 指标 | 数值 |
|------|------|
| 自动化用例总数 | **142** |
| 通过 | **142** |
| 失败 | **0** |
| 跳过 | **0** |
| 自检脚本 | `scripts/verify.ps1` → **OK: verify passed** |

---

## 2. 分模块结果

| 模块 | 测试类数 | Tests run | Failures | 说明 |
|------|----------|-----------|----------|------|
| jieqi-core | 28 | 89 | 0 | 规则、协议、棋谱、复盘 |
| jieqi-ai | 9 | 16 | 0 | 搜索、评估、Bot、Agent |
| jieqi-server | 12 | 37 | 0 | TCP 集成 + WS 集成（21 场景）+ 复盘落盘 |
| jieqi-client | 0 | — | — | 无独立单测（由 WS 集成覆盖） |
| jieqi-app | 0 | — | — | 启动器，无单测 |

---

## 3. 关键用例执行结果（摘录）

| 编号 | 用例 | 结果 | 备注 |
|------|------|------|------|
| R11–R13 | 送将/照面/解将 | 通过 | `RuleEdgeCaseTest` 11 项全绿 |
| E01–E04 | 和棋/长将/长捉 | 通过 | 含兵卒长捉和 |
| A03 | AI 不透视 | 通过 | `AiFairnessTest` |
| A06 | 搜索棋盘一致 | 通过 | `BoardUndoTest` |
| N01 | WS 匹配开局 | 通过 | `WsGameServerIntegrationTest` |
| P04 | 复盘帧请求 | 通过 | resign 后 replayRequest + `.replay.json` |
| P05 | 观战 watch | 通过 | `watchJoinsActiveGameAsObserver` |
| B02 | 全量 mvn test | 通过 | BUILD SUCCESS |

完整清单见 [TEST_CASES.md](./TEST_CASES.md)。

---

## 4. AI 性能观测（单次本地运行，非严格基准）

| 等级 | 典型步时 | 超时次数 | 观测深度/节点（日志） |
|------|----------|----------|------------------------|
| Easy | < 500 ms | 0 | 无深度搜索 |
| Medium | 1–3 s | 0 | 深度约 8–12，节点 10⁴–10⁵ 级 |
| Hard | 2–5 s | 0 | 多采样 + 浅层 AB，节点因采样分摊 |

正式答辩以现场演示为准；Hard 在复杂中局可能提前降级 fallback。

---

## 5. 失败用例详情

**无**（当前 `main` / `feat/youhua` 工作区全绿）。

---

## 6. 已知未修 / 待强化项

| 优先级 | 项 | 说明 |
|--------|-----|------|
| P2 | 走子错误原因码 | `RuleValidator` 仍多为 boolean |
| P2 | 长捉复杂分类 | 人工复核极端局面 |
| P3 | jieqi-client 单测 | 依赖集成测试间接覆盖 |

---

## 7. 手动测试记录

历史 TCP/WS 手动记录已归档清理；答辩演示以 [DEMO_SCRIPT.md](../07-presentation/DEMO_SCRIPT.md) 为准。

---

*本报告随 `mvn test` 输出更新 · v1.0 · 2026-06-18*

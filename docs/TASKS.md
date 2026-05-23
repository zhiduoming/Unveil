# 任务看板（监工同步）

> **最后更新**：2026-05-22（第十轮 T37–T40）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T36 | 见历史轮次 | 已完成 | |
| **T37** | `FrameDecoder` payload/缓冲上限测试 | 已完成 | Q25/Q26 对齐 |
| **T38** | 提和接受和棋集成测试 | 已完成 | `GameServerDrawIntegrationTest` |
| **T39** | `ProbabilityAgent` 单元测试 | 已完成 | 期望值 bias |
| **T40** | `AgentOrchestrator` 编排测试 | 已完成 | stub Agent 选着 |

## 第十轮提交记录（`befaee6` …）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `befaee6` | test(protocol): FrameDecoder limits |
| 2 | `a94baca` | test(server+ai): draw IT + Agent tests |
| 3 | `90e7e3e` | docs: TASKS INTEROP REPORT |

## 监工汇报（第十轮）

- **T37**：超长声明长度与缓冲超限拒绝/抛错。
- **T38**：红提和 → 黑收 OFFER → 黑 ACCEPT → `GAME_OVER` winner=-1、原因码 9。
- **T39**：`ProbabilityAgent` 写入与棋盘 EV 差一致的 bias，不直接选着。
- **T40**：自定义 stub Agent 优先返回走法，验证编排链。
- **验证**：40 tests · `scripts/verify.ps1`

## 第九轮提交记录（`e786cec` … `5593c1c`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `e786cec` | test(server): illegal move + resign IT |
| 2 | `0e9fdf4` | test(ai): EnhancedEvaluator |
| 3 | `9e417e4` | chore(scripts): verify package |
| 4 | `bfe5bf4` | docs: round 9 |

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T37–T40 | 高 |

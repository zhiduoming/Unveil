# 任务看板（监工同步）

> **最后更新**：2026-05-22（第九轮 T33–T36）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T32 | 见历史轮次 | 已完成 | |
| **T33** | 非法着法 ERROR 101 集成测试 | 已完成 | `GameServerIllegalMoveIntegrationTest` |
| **T34** | 认输 GAME_OVER + 棋谱落盘 IT | 已完成 | `GameServerResignIntegrationTest` |
| **T35** | `EnhancedEvaluator` 单元测试 | 已完成 | 评估反对称性 |
| **T36** | `verify.ps1` 增加打包步骤 | 已完成 | 与 CI 对齐 |

## 第九轮提交记录（`e786cec` …）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `e786cec` | test(server): illegal move + resign IT |
| 2 | `0e9fdf4` | test(ai): EnhancedEvaluator |
| 3 | `9e417e4` | chore(scripts): verify package |
| 4 | — | docs: TASKS INTEROP REPORT |

## 监工汇报（第九轮）

- **T33**：黑方在红方回合走子 → 收到 `ERROR|101|…`。
- **T34**：红方走一着后认输 → 黑方 `GAME_OVER` 原因码 `RESIGN(3)`，棋谱落盘含该着法。
- **T35**：`EnhancedEvaluator` 红黑视角评估值互为相反数。
- **T36**：`verify.ps1` 在 compile 后执行 `mvn package -pl jieqi-app -am -DskipTests`。
- **验证**：35 tests · `scripts/verify.ps1`

## 第八轮提交记录（`6325634` … `4bad26b`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `6325634` | feat(record): GameRecord import |
| 2 | `56cf33a` | feat(server): GameRecordStore.load |
| 3 | `5b88e6b` | test(record): import 往返 |
| 4 | `a8aa468` | test(server): MOVE IT + 基类 |
| 5 | `8def381` | ci: package jieqi-app |
| 6 | `24f0b5b` | docs: round 8 |

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T33–T36 | 高 |

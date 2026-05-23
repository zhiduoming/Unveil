# 任务看板（监工同步）

> **最后更新**：2026-05-22（第七轮 T23–T27）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T22 | 见历史轮次 | 已完成 | |
| **T23** | `Coordinate` 坐标类 + 校验测试 | 已完成 | `ChessPiece` 委托 |
| **T24** | `MoveNotation.parse` 棋谱解析 | 已完成 | 往返测试 |
| **T25** | 暗子期望值 `getExpectedValue` 测试 | 已完成 | `BoardExpectedValueTest` |
| **T26** | 双客户端 LOGIN 集成测试 | 已完成 | `GameServerLoginIntegrationTest` |
| **T27** | 文档同步（REPORT/README/INTEROP） | 已完成 | verify.ps1、28 tests |

## 第七轮提交记录（`c718f2e` …）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `c718f2e` | feat(core): Coordinate |
| 2 | `c5ec56d` | feat(record): MoveNotation.parse |
| 3 | `6719ce0` | test(core): Coordinate / EV / notation parse |
| 4 | `f009751` | test(server): LOGIN 集成测试 |
| 5 | `aabeef0` | docs: REPORT README INTEROP TASKS |

## 监工汇报（第七轮）

- **T23**：独立 `Coordinate`（a–i / 0–9），`ChessPiece.toCoord/fromCoord` 委托。
- **T24**：棋谱单行解析，与 `format` 往返一致。
- **T25**：开局暗子池平均子力期望值为正且红黑对称。
- **T26**：随机端口起服，双 Socket LOGIN → LOGIN_ACK → 双方到齐 `GAME_START`。
- **T27**：README 增加 `verify.ps1`；REPORT/INTEROP 更新测试数与联调项。
- **验证**：28 tests · `scripts/verify.ps1`

## 第六轮提交记录（`a9b51a5` … `1f58b7c`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `a9b51a5` | feat(core): MoveNotation §6.3 |
| 2 | `dbd8a02` | test(core): GameRecord + 协议帧集成测试 |
| 3 | `598bca4` | test(ai): JieqiAgent 单元测试 |
| 4 | `580a7f0` | ci: GitHub Actions |
| 5 | `c5dd67f` | chore(scripts): verify.ps1 含 jieqi-ai |
| 6 | `1f58b7c` | docs: TASKS 第六轮 |

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T23–T27 | 高 |
| 2026-05-22 | T19–T22 | 高 |

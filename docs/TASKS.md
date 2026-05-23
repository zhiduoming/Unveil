# 任务看板（监工同步）

> **最后更新**：2026-05-22（第六轮 T19–T22）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T18 | 见历史轮次 | 已完成 | |
| **T19** | 棋谱记法 `MoveNotation`（INTERFACE §6.3） | 已完成 | `Move` 委托、`GameRecord` |
| **T20** | `JieqiAgentTest` | 已完成 | `jieqi-ai` JUnit |
| **T21** | GitHub Actions CI | 已完成 | `.github/workflows/ci.yml` |
| **T22** | 协议帧集成测试 | 已完成 | `ProtocolFrameIntegrationTest` |

## 第六轮提交记录（`a9b51a5` … `c5dd67f`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `a9b51a5` | feat(core): MoveNotation §6.3 |
| 2 | `dbd8a02` | test(core): GameRecord + 协议帧集成测试 |
| 3 | `598bca4` | test(ai): JieqiAgent 单元测试 |
| 4 | `580a7f0` | ci: GitHub Actions |
| 5 | `c5dd67f` | chore(scripts): verify.ps1 含 jieqi-ai |

## 监工汇报（第六轮）

- **T19**：`MoveNotation.format` 输出 `source-dest[(type)][翻]`；`Move.toNotation()` 委托；空棋谱占位写入 `GameRecordStore`。
- **T20**：`JieqiAgentTest` 验证开局可出招（SearchAgent 链路）。
- **T21**：push/PR 触发 `mvn test`（core/server/ai）+ 全模块 `compile`。
- **T22**：`ProtocolFrameIntegrationTest` 编解码往返；`GameRecordTest` 记法与行格式。
- **验证**：22 tests · `scripts/verify.ps1`

## 第五轮提交记录（`bf5bf17` … `5264567`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `bf5bf17` | feat(core): Game 匹配辅助方法 |
| 2 | `0b41364` | feat(server): MatchmakingService |
| 3 | `c25a579` | feat(server): 接入匹配与终局清理 |
| 4 | `c307f21` | test(server): MatchmakingService |
| 5 | `636328e` | docs: INTEROP.md |
| 6 | `5264567` | docs: TASKS 第五轮 |

## 监工汇报（第五轮）

- **T17**：优先撮合 1/2 房间；`new`/`*` 强制新建；已开始/已满返回 ERROR 200/201；终局后移除内存房间。
- **T18**：`INTEROP.md` 联调步骤、记录表、自检结果。
- **验证**：17 tests · `scripts/verify.ps1`

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T19–T22 | 高 |
| 2026-05-23 | T17–T18 | 高 |

# 任务看板（监工同步）

> **最后更新**：2026-05-22（第八轮 T28–T32）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T27 | 见历史轮次 | 已完成 | |
| **T28** | 棋谱导入 `GameRecord.fromExportedLines` | 已完成 | 支持序号行与 `#` 头 |
| **T29** | `GameRecordStore.load` 往返 | 已完成 | `GameRecordStoreTest` |
| **T30** | 走子 MOVE 集成测试 | 已完成 | `GameServerMoveIntegrationTest` |
| **T31** | 集成测试基类抽取 | 已完成 | `AbstractGameServerIntegrationTest` |
| **T32** | CI 增加 `jieqi-app` 打包 | 已完成 | `.github/workflows/ci.yml` |

## 第八轮提交记录（`6325634` …）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `6325634` | feat(record): GameRecord import |
| 2 | `56cf33a` | feat(server): GameRecordStore.load |
| 3 | `5b88e6b` | test(record): import 往返 |
| 4 | `a8aa468` | test(server): MOVE IT + 基类 |
| 5 | `8def381` | ci: package jieqi-app |
| 6 | `24f0b5b` | docs: TASKS REPORT INTEROP |

## 监工汇报（第八轮）

- **T28–T29**：`.jieqi` 文件可解析回 `GameRecord`（跳过注释与空着法占位）。
- **T30**：红方首着合法走子后，黑方收到 `MSG_MOVE` 广播且坐标一致。
- **T31**：LOGIN/MOVE 集成测试共用基类（起服、登录、帧等待、首着搜索）。
- **T32**：CI 在 test+compile 后执行 `mvn package -pl jieqi-app -am -DskipTests`。
- **验证**：32 tests · `scripts/verify.ps1`

## 第七轮提交记录（`c718f2e` … `48dc332`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `c718f2e` | feat(core): Coordinate |
| 2 | `c5ec56d` | feat(record): MoveNotation.parse |
| 3 | `6719ce0` | test(core): Coordinate / EV / notation parse |
| 4 | `f009751` | test(server): LOGIN 集成测试 |
| 5 | `aabeef0` | docs: REPORT README INTEROP TASKS |
| 6 | `48dc332` | docs: round-7 hash |

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T28–T32 | 高 |

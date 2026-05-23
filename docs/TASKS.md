# 任务看板（监工同步）

> **最后更新**：2026-05-22（第二轮 T6–T9）— 请 `git pull` 查看提交与本文。

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1 | FrameDecoder / ProtocolReader | 已完成 | |
| T2 | BOARD_STATE 棋盘同步 | 已完成 | |
| T3 | server/client 字节帧读取 | 已完成 | |
| T4 | 单元测试 | 已完成 | 7 tests |
| T5 | Docker / compose | 已完成 | |
| T6 | RandomRevealService 权威翻子 | 已完成 | 服务端清除客户端 type |
| T7 | GameRecord 落盘 | 已完成 | `records/{gameId}.jieqi` |
| T8 | 多 Agent AI 编排 | 已完成 | `JieqiAgent` + 3 子 Agent |
| T9 | 组间联调自检 | 已完成 | INTERFACE §13 已勾选 |
| T10 | 实验报告 + TEAM 分工 | 进行中 | `REPORT.md` 草稿已建 |

## 第二轮提交记录（`5541afd` … `def63b8`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `5541afd` | feat(core): RandomRevealService + 测试 |
| 2 | `fd00cc7` | feat(core): Game 集成 GameRecord |
| 3 | `2eceb69` | feat(server): GameRecordStore 落盘 |
| 4 | `09c692f` | feat(server): ClientHandler 翻子权威 + 触发保存 |
| 5 | `aa30b24` | feat(ai): 多 Agent + JieqiAgent |
| 6 | `dcf7c54` | docs: 联调清单 + REPORT 草稿 |
| 7 | `def63b8` | chore: gitignore records/ |

## 监工汇报（第二轮）

**本轮完成：T6–T9，T10 草稿**

- **T6**：`RandomRevealService` — 忽略客户端 type，广播前 `stampServerRevealType`。
- **T7**：`Game` 使用 `GameRecord`；`GameRecordStore` 在对局结束时写入 `records/`。
- **T8**：`ProbabilityAgent` → `EndgameAgent` → `SearchAgent`，`JieqiAgent` 对外接口。
- **T9**：`INTERFACE.md` §13 本组实现项已全部勾选。
- **T10**：`docs/REPORT.md` 骨架，待填分工与 AI 使用说明。

**验证**：`mvn test -pl jieqi-core` · `mvn compile`

**下一步**：完善 `TEAM.md` / `REPORT.md` 正文；与他组联调实战。

## 监工留言区

（监工追加任务请写在此。）

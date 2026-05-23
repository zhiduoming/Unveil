# 任务看板（监工同步）

> **最后更新**：2026-05-23（第三轮 T10 + 规则补全）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T5 | 网络帧 / Docker 等 | 已完成 | 见第一轮提交 |
| T6–T9 | 翻子 / 棋谱 / AI / 联调清单 | 已完成 | 见第二轮提交 |
| T10 | 实验报告 + TEAM 分工 | 已完成 | `REPORT.md` + `TEAM.md` |
| T11 | 暗子士象规则 + 坐标测试 | 已完成 | `RuleValidator` + JUnit |

## 第三轮提交记录（`2411a21` … `2a1501d`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `2411a21` | fix(core): 暗子士象九宫/过河 |
| 2 | `74e491a` | test(core): 坐标与暗子规则 |
| 3 | `ea8bffe` | test(server): GameRecordStore |
| 4 | `c1431d1` | feat(ai): EnhancedAIEngine 协议 v2 |
| 5 | `ecfdb0a` | docs: TEAM 分工 |
| 6 | `a675f87` | docs: REPORT 正文 |
| 7 | `c693c6c` | docs: README |
| 8 | `2a1501d` | docs: TASKS 第三轮 |

## 监工汇报（第三轮）

- **T10**：四人分工与贡献度；实验报告正文（架构、粘包、翻子、多 Agent、测试）。
- **T11**：暗子士限九宫/象不过河；明子强化；坐标与 `GameRecordStore` 单测；AI 引擎改用 `ProtocolReader`。
- **验证**：`mvn test -pl jieqi-core,jieqi-server` 全部通过。

## 监工留言区

（暂无新任务。）

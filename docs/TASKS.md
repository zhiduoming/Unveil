# 任务看板（监工同步）

> **最后更新**：2026-05-29（协议 v3.0：WebSocket JSON 正文 + TCP 附录 B）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| **WS** | 老师公共接口 WebSocket + JSON 实现 | 已完成 | `WsGameServer` / `WsGameClient` |
| **DOC** | INTERFACE.typ v3.0 与全文档同步 | 已完成 | TCP v2.0 降为附录 B |
| **TCP** | 原 TCP v2.0 保留为可选扩展 | 已完成 | 端口 8888，测试仍通过 |

## 提交记录（本轮）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `d3fb789` | feat(core): EndgameJudge 长将长捉 |
| 2 | `8f2dd29` | test(core): 终局五场景 |
| 3 | `036fb99` | docs: CLAUDE REQUIREMENTS TASKS |

## 监工汇报（总监任务）

- **#2**：同局面第 6 次重复时：将军 → 走子方（将军方）`REPETITION_LOSS`；非兵卒长捉 → 走子方判负；兵卒长捉 → `REPETITION_DRAW`；无将无捉重复不自动终局。吃子清空重复计数；局面哈希含轮到谁走。
- **#9**：`EndgameJudgeTest` + `GameEndgameTest` 覆盖五类终局。
- **#10**：全仓库测试通过；`CLAUDE.md` 补充 `com.jieqi.record`、`com.jieqi.ai.agent`。
- **#6**：`CLAUDE.md` Git 规范明确禁用 chore/ci。

## 第十一轮提交记录（`d52fe0b` … `a30c04a`）

见历史轮次。

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | 总监 #2/#9/#10/#6 | 高 |

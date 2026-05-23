# 任务看板（监工同步）

> **最后更新**：2026-05-22（第十一轮 T41–T45）

## 当前迭代

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1–T40 | 见历史轮次 | 已完成 | |
| **T41** | `Protocol` Move 序列化往返测试 | 已完成 | `ProtocolMoveSerializationTest` |
| **T42** | `TURN_CHANGE` 集成测试 | 已完成 | 走子后广播 |
| **T43** | 未知 msgType 忽略 + 仍可走子 | 已完成 | `GameServerUnknownMsgIntegrationTest` |
| **T44** | CHAT 广播与 10s 限速 | 已完成 | `GameServerChatIntegrationTest` |
| **T45** | `EndgameAgent` 开局不激活 | 已完成 | 子力阈值 |

## 第十一轮提交记录（`d52fe0b` …）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `d52fe0b` | test(protocol): Move serialization |
| 2 | `bc527e0` | test(server): turn/unknown/chat IT |
| 3 | `27babb6` | test(ai): EndgameAgent |
| 4 | — | docs + README |

## 监工汇报（第十一轮）

- **T41**：`serializeMove` / `deserializeMove` 往返含 type、时间戳、翻子标志。
- **T42**：红方合法走子后双方收到 `TURN_CHANGE|1`（黑方回合）。
- **T43**：发送 msgType=99 不崩溃，后续 MOVE 仍广播。
- **T44**：CHAT 转发对手；10 秒内第二条返回 ERROR 限速提示。
- **T45**：满盘时 `EndgameAgent.supports` 为 false。
- **验证**：46 tests · `scripts/verify.ps1`

## 第十轮提交记录（`befaee6` … `0d6a475`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `befaee6` | test(protocol): FrameDecoder limits |
| 2 | `a94baca` | test(server+ai): draw IT + Agent tests |
| 3 | `90e7e3e` | docs: round 10 |

## 监工留言区

| 时间 | 任务 | 优先级 |
|------|------|--------|
| 2026-05-22 | T41–T45 | 高 |

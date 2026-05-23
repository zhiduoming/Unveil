# 组间联调记录（T18）

> 协议版本：**v2.0** · 默认端口 **8888** · 帧格式 `msgType|payloadLen|payload\n`

## 1. 联调前自检

在本组环境执行：

```powershell
powershell -File scripts/verify.ps1
```

对照 [INTERFACE.md](./INTERFACE.md) 第 13 节（本组实现项已勾选）。

## 2. 多盘对弈（gameId）约定

| LOGIN 第三段 `gameId` | 行为 |
|----------------------|------|
| （空） | 优先加入已有 1 人的 `WAITING` 房间，否则新建 |
| 具体 ID（如 `a1b2c3d4`） | 加入该房间；不存在 → ERROR 200 |
| `new` 或 `*` | 强制新建房间 |

对局结束后服务器从内存移除该 `gameId`，可复用 ID 与别组合联调（需重新 LOGIN）。

服务器日志前缀：`[Match]`。

## 3. 与他组联调步骤

1. 约定统一端口（默认 8888）与协议 PDF/`INTERFACE.md`。  
2. A 组起 server：`mvn exec:java -pl jieqi-app -Dexec.args="server 8888"`  
3. B 组起 client 连 A 组 IP，或双方均用本仓库 client。  
4. 一方 LOGIN 留空 `gameId`，另一方留空或填对方 `LOGIN_ACK` 中的 `gameId`。  
5. 走子 3～5 步，验证 MOVE / BOARD_STATE / TURN_CHANGE。  
6. 测试 ERROR（非法着）、CHAT、认输、和棋（可选）。

## 4. 联调记录表（填写）

| 日期 | 对方小组 | 对方 server/client | 结果 | 问题与处理 |
|------|----------|-------------------|------|------------|
| | | | ☐ 通过 ☐ 失败 | |

## 5. 本组自检结果（2026-05-23）

| 项目 | 结果 |
|------|------|
| 帧格式与 payloadLen | 通过（`FrameDecoder`） |
| BOARD_STATE 重建 | 通过 |
| 服务器权威翻子 type | 通过 |
| 多盘 gameId 匹配 | 通过（`MatchmakingService`） |
| 双端 TCP 登录集成测试 | 通过（`GameServerLoginIntegrationTest`） |
| 双端本地对战（本仓库 client×2） | 待与他组交叉验证 |

## 6. 已知差异（开放问题）

见 `INTERFACE.typ` 第十章 Q1–Q44，裁定前以本组方案为准。

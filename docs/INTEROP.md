# 组间联调记录（T18）

> 协议版本：**v3.0** · 主协议 **WebSocket JSON** · 默认端口 **8887**  
> 可选附录：**TCP v2.0** · 端口 **8888**

## 1. 联调前自检

```powershell
powershell -File scripts/verify.ps1
```

对照 [INTERFACE.md](./INTERFACE.md) §8 组间联调清单。

## 2. WebSocket 联调步骤（推荐）

1. 约定使用 **WebSocket JSON**（INTERFACE v3.0 正文）与端口 **8887**。  
2. A 组起 server：  
   `mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"`  
3. B 组起 client：  
   `mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://<A组IP>:8887 userB pass"`  
4. 双方：`match` → `ready` → 走子 3～5 步，验证 `moveResult` / `gameStart.initialBoard`。  
5. 测试非法着法（`valid=false`）、认输（`gameOver`）、可选 `ping`。

## 3. TCP 扩展联调（附录 B，可选）

若对方仍使用本组 v2.0 TCP 协议：

| 项目 | 约定 |
|------|------|
| 端口 | 8888 |
| 帧格式 | `msgType\|payloadLen\|payload\n` |
| 多盘 | LOGIN 第三段 `gameId`（空=匹配） |

步骤见历史记录；实现类 `GameServer` / `GameClient`。

## 4. 联调记录表

| 日期 | 对方小组 | 协议 (WS/TCP) | 结果 | 问题与处理 |
|------|----------|---------------|------|------------|
| | | | ☐ 通过 ☐ 失败 | |

## 5. 本组自检结果

### WebSocket JSON（v3.0）

| 项目 | 结果 |
|------|------|
| Login / loginResult | 通过 |
| startMatch / matchSuccess | 通过 |
| Ready / gameStart | 通过 |
| move / moveResult + flipResult | 通过 |
| ping / pong | 通过 |
| 非法着法 error 2001 | 通过（逻辑层） |
| 集成测试 | 通过（`WsGameServerIntegrationTest`） |
| 与他组交叉验证 | 待填 |

### TCP v2.0（附录 B，2026-05-23）

| 项目 | 结果 |
|------|------|
| FrameDecoder / BOARD_STATE | 通过 |
| 9 项 GameServer 集成测试 | 通过 |
| 与他组交叉验证 | 待填 |

## 6. 已知差异

见 `INTERFACE.typ` 第十章 Q1–Q44。v3.0 正文对齐课程公共接口；本组扩展 `gameOver.reason` 字符串（如 `king_captured`）需在联调时说明。

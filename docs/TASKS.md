# 任务看板（监工同步）

> 状态：`待办` | `进行中` | `已完成` | `阻塞`  
> **最后更新**：2026-05-22 — 请监工 `git pull` 后查看本文件与下方提交记录。

## 当前迭代（协议 v2.0 + 工程化）

| ID | 任务 | 状态 | 备注 |
|----|------|------|------|
| T1 | `FrameDecoder` / `ProtocolReader` 粘包半包 | 已完成 | Q25 |
| T2 | `BOARD_STATE` 客户端重建棋盘 | 已完成 | `Board.syncFromBoardStatePayload` |
| T3 | server/client 接入字节帧读取 | 已完成 | 替代 `readLine` |
| T4 | 单元测试（FrameDecoder + 棋盘同步） | 已完成 | `mvn test -pl jieqi-core` |
| T5 | Docker / docker-compose | 已完成 | `docker compose up --build` |
| T6 | `RandomRevealService` 服务器权威翻子 | 待办 | |
| T7 | `GameRecord` 落盘或导出 | 待办 | |
| T8 | 多 Agent AI 编排 | 待办 | Q33–Q34 |
| T9 | 组间联调自检 | 待办 | `INTERFACE.md` §13 |
| T10 | 实验报告 + `TEAM.md` 分工 | 待办 | |

## 本轮提交记录（`731816f` … `c6c9aa2`）

| # | Commit | 说明 |
|---|--------|------|
| 1 | `731816f` | feat(core): FrameDecoder + ProtocolReader |
| 2 | `57bb9e6` | feat(core): BOARD_STATE 棋盘重建 |
| 3 | `3a26f2d` | fix(core): BOARD_STATE 行间分隔符 `\|` |
| 4 | `994cf9a` | test(core): JUnit 单测 |
| 5 | `e16be3f` | feat(server): ProtocolReader |
| 6 | `8b267b1` | feat(client): ProtocolReader + 棋盘同步 |
| 7 | `9123a7a` | chore: .gitignore |
| 8 | `c6c9aa2` | chore: Dockerfile + docker-compose |

## 监工汇报（本轮摘要）

**已完成（T1–T5）**

1. **网络帧解码**：`FrameDecoder` 字节缓冲 + `payloadLen` 校验，解决粘包/半包；`ProtocolReader` 阻塞读帧。
2. **棋盘同步**：`Board.syncFromBoardStatePayload`；客户端收到 `BOARD_STATE` 后全量重建棋盘。
3. **双端接入**：`ClientHandler` / `GameClient` 不再依赖 `readLine()`。
4. **测试**：5 个用例（含粘包、半包、错误长度、棋盘往返）。
5. **部署**：根目录 `Dockerfile` + `docker-compose.yml`。

**验证命令**

```bash
mvn test -pl jieqi-core
mvn compile
docker compose up --build   # 需本机 Docker
```

**下一步（建议监工排期）**

- T6 → T7 → T9（翻子服务、棋谱、联调）

## 待老师裁定

`docs/INTERFACE.typ` 第十章 Q1–Q44。

## 监工留言区

（监工在此追加新任务行即可。）

# 揭棋对弈程序 — 实验报告（草稿）

> Typst 正式版可由此 Markdown 迁移。提交前请填写 `TEAM.md` 分工与贡献度。

## 1. 小组与分工

见 [TEAM.md](./TEAM.md)。

## 2. 需求与协议

- 功能需求：[REQUIREMENTS.md](./REQUIREMENTS.md)
- 公共协议 v2.0：[INTERFACE.md](./INTERFACE.md) / `INTERFACE.pdf`
- 开放问题 Q1–Q44：INTERFACE 第十章

## 3. 架构与模块

见 [ARCHITECTURE.md](./ARCHITECTURE.md)。Maven 模块：`jieqi-core` / `server` / `client` / `ai` / `app`。

## 4. 关键技术说明（待扩写）

### 4.1 TCP 粘包/半包

`FrameDecoder` + `ProtocolReader`：字节缓冲、按 `payloadLen` 校验 UTF-8 长度。

### 4.2 服务器权威翻子

`RandomRevealService`：清除客户端 type，走子后以棋盘真实类型写回广播 MOVE。

### 4.3 多 Agent AI

`ProbabilityAgent` → `EndgameAgent`（可选）→ `SearchAgent`，由 `JieqiAgent` 统一对外。

## 5. 测试与联调

```bash
mvn test -pl jieqi-core
mvn compile
```

组间自检：INTERFACE.md §13（本组已勾选实现项）。

## 6. AI 辅助使用说明

（描述如何用 AI 完成需求分析、协议设计、编码与测试。）

## 7. 总结与展望

（待填写。）

# Unveil 揭棋对弈系统 — 最终报告

> **课程**：揭棋对弈程序设计 · 北京邮电大学  
> **团队**：第一组（组长 张恒基）  
> **版本**：v1.0 · 2026-06-18  
> **说明**：本文整合各专题设计文档，细节以链接子文档为准。

**文档导航**：[docs/README.md](../README.md)

---

## 1. 项目简介

### 1.1 项目背景

揭棋是中国象棋变体：开局仅将帅明置，其余棋子暗置并按**位置角色**走子；移动或吃子后翻开，明子按真实身份行棋。规则含禁送将、照面、长将长捉、40 步无吃子和等，适合作为网络对弈 + 规则引擎 + AI 博弈的综合课设。

### 1.2 项目目标

构建可验收的揭棋对弈系统：**服务端权威规则**、**WebSocket 互操作**、**三档 AI**、**棋谱与复盘**、**Maven 工程化**。

### 1.3 核心功能

| # | 功能 |
|---|------|
| 1 | 揭棋规则引擎（`RuleValidator` / `EndgameJudge`） |
| 2 | WebSocket JSON 真人对弈（端口 8887） |
| 3 | TCP 文本帧兼容（附录 B，8888） |
| 4 | Easy / Medium / Hard 三档 AI |
| 5 | 文字棋谱 + 复盘时间线 + JSON 落盘 |
| 6 | 自检脚本 `verify.ps1`、演示脚本 `demo.ps1` |

### 1.4 技术栈

Java 21 · Maven · Java-WebSocket · Gson · JUnit 5 · Docker Compose · Typst（协议 PDF）

---

## 2. 需求分析

详见 [01-requirements/REQUIREMENTS.md](../01-requirements/REQUIREMENTS.md)、[ACCEPTANCE_CRITERIA.md](../01-requirements/ACCEPTANCE_CRITERIA.md)。

| 优先级 | 范围 |
|--------|------|
| P0 | 规则正确、WS 对弈、非法走法拒绝、超时、棋谱 |
| P1 | 三档 AI、复盘、终局摘要、演示流程 |
| P2 | 观战、错误码细化、Web GUI |

**用户角色**：普通玩家、AI 对手、观战者（实验）、教师验收者。

**非功能**：单步 AI < 5s（可配置）；`mvn test` 全绿；Docker 可启动 WS 服务。

---

## 3. 总体架构

详见 [02-design/ARCHITECTURE.md](../02-design/ARCHITECTURE.md)。

```
jieqi-core  ← 领域、规则、协议模型
    ↑
jieqi-server / jieqi-client / jieqi-ai
    ↑
jieqi-app（启动入口）
```

对局主路径：`move` → `Game.processMove` → `Board.executeMove` → `EndgameJudge` → `moveResult` / `gameOver`。

---

## 4. 规则引擎设计

详见 [RULE_ENGINE_DESIGN.md](../02-design/RULE_ENGINE_DESIGN.md)。

| 要点 | 实现 |
|------|------|
| 坐标 | 显示 a–i / 0–9；内部 `row=9-displayRow` |
| 暗子 | `virtualType` 走子；翻开写 `type` |
| 校验链 | 轮次 → 超时 → 禁止原地翻子 → `isValidMove` → `isMoveLegal` |
| 终局 | 将死、困毙、吃将、和棋、长将长捉 |

测试：`RuleEdgeCaseTest`（11 项）、`EndgameJudgeTest`。

---

## 5. 网络通信设计

协议权威：[INTERFACE.typ](../INTERFACE.typ) v3.0。

| 通道 | 端口 | 用途 |
|------|------|------|
| WebSocket JSON | 8887 | 课程主协议、验收默认 |
| TCP 文本帧 | 8888 | 附录 B、调试 |

房间生命周期：Login → match → Ready → gameStart → move 循环 → gameOver →（可选 rematch）。

消息示例：[03-interface/MESSAGE_EXAMPLES.md](../03-interface/MESSAGE_EXAMPLES.md)。

---

## 6. AI 算法设计

详见 [AI_DESIGN.md](../02-design/AI_DESIGN.md)。

| 档位 | 策略 | 时间 |
|------|------|------|
| Easy | 启发式 + 随机 | < 500 ms |
| Medium | Alpha-Beta + 置换表 | ~5 s |
| Hard | Belief Sampling + AB | ~5 s |

约束：不透视对手暗子；超时 fallback；`AiFairnessTest` 验证。

---

## 7. 客户端设计

- **WsGameClient**：match / move / chat / replay / rematch / ai  
- **ConsoleUI**：10×9 棋盘，`?` 表示暗子  
- **Main 菜单**：1–9 模式（WS 服务器/客户端、本地人机、AI 自动对弈等）

产品闭环：终局摘要 → `replay` 复盘 → `rematch`（见 [USER_JOURNEY.md](../06-product/USER_JOURNEY.md)）。

---

## 8. 棋谱与复盘

详见 [REPLAY_DESIGN.md](../02-design/REPLAY_DESIGN.md)。

| 产物 | 路径 |
|------|------|
| 文字棋谱 | `records/<id>.jieqi` |
| 复盘 JSON | `records/<id>.replay.json` |

协议扩展：`replayRequest` / `replayFrame`。

---

## 9. 接口协议

| 类别 | 消息示例 |
|------|----------|
| 课程公共 | Login, startMatch, Ready, move, gameStart, moveResult, gameOver |
| 本组扩展 | replayRequest, replayFrame, rematchRequest, addTime |

编译 PDF：`typst compile docs/INTERFACE.typ docs/INTERFACE.pdf`

---

## 10. 测试方案与结果

| 文档 | 内容 |
|------|------|
| [TEST_PLAN.md](../05-testing/TEST_PLAN.md) | 四层测试策略 |
| [TEST_CASES.md](../05-testing/TEST_CASES.md) | 45 条用例 |
| [TEST_REPORT.md](../05-testing/TEST_REPORT.md) | **142/142 通过** |

---

## 11. 部署与运行

详见 [BUILD_AND_RUN.md](../04-deployment/BUILD_AND_RUN.md)。

```powershell
powershell -File scripts/verify.ps1
powershell -File scripts/demo.ps1
```

Docker：`docker compose up --build`（映射 8887）。

---

## 12. 项目管理

分工见 [TEAM.md](../TEAM.md)。

| 成员 | 主要负责 |
|------|----------|
| 张恒基 | 架构、协议、AI、文档 |
| （其他成员） | 见 TEAM.md 贡献度表 |

开发过程使用 AI 辅助分析、测试生成与文档起草；核心规则与协议经人工评审与单测验证。

---

## 13. 总结与展望

### 13.1 已完成功能

规则引擎、WS/TCP 双栈、三档 AI、棋谱与复盘、Maven 多模块、自检与演示脚本、六类文档体系（24 份规划，核心 ★★★ 已交付）。

### 13.2 已知限制

- 走子错误未细分原因码（⚡）  
- Hard AI 复杂局面深度受限（⚡）  
- 观战、Web GUI 为规划项（📋）  
- 长捉极端分类需人工复核（⚡）

### 13.3 后续规划

v1.1：错误码、观战服务端；v2.0：Web 旁观与排行榜（见 [PRODUCT_REQUIREMENTS.md](../06-product/PRODUCT_REQUIREMENTS.md)）。

---

## 附录：功能完成度摘要

完整矩阵见 [FEATURE_MATRIX.md](../00-overview/FEATURE_MATRIX.md)。

| 状态 | 约计 |
|------|------|
| ✅ 已实现 | 48 项 |
| ⚡ 待强化 | 12 项 |
| 🔬 实验性 | 1 项 |
| 📋 规划中 | 4 项 |

---

*本报告为答辩与提交主文档；演示现场请配合 [DEMO_SCRIPT.md](./DEMO_SCRIPT.md) 与 [DEFENSE_QA.md](./DEFENSE_QA.md)。*

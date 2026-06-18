# 答辩问答预演

> **关联**：[DEMO_SCRIPT.md](./DEMO_SCRIPT.md) · [AI_DESIGN.md](../02-design/AI_DESIGN.md) · [RULE_ENGINE_DESIGN.md](../02-design/RULE_ENGINE_DESIGN.md)

格式：**Q** → **推荐回答**（简洁、可背诵）

---

## 架构类

**Q1：为什么分 5 个 Maven 模块？**  
**A**：`jieqi-core` 放领域与规则，不依赖网络；`server`/`client`/`ai` 分别负责通信与博弈；`app` 只做启动聚合。这样规则只维护一份，AI 与服务器都调用同一套 `Game`/`Board`，符合单一职责与课程面向对象要求。

**Q2：某个模块挂了，其他模块受影响吗？**  
**A**：`core` 可独立单测；服务器进程挂掉只影响在线对局，客户端与 AI 库仍可本地运行；AI 搜索异常有 `selectWithFallback` 退回合法首步，不会拖垮服务器。

**Q3：为什么用 WebSocket 而不是 HTTP？**  
**A**：对弈是双向实时推送（走子、计时、聊天），WebSocket 全双工、低开销；课程 2026 公共接口也指定 JSON over WebSocket。

---

## 规则类

**Q4：如何保证规则正确？**  
**A**：三层：① `RuleValidator` 几何规则 + `isMoveLegal` 试走防送将；② `EndgameJudge` 统一终局；③ 自动化测试 `RuleEdgeCaseTest`（11 项边界）+ 集成测试拒绝非法走法。

**Q5：规则校验在客户端还是服务器？**  
**A**：**服务器权威**。`Game.processMove` 为唯一入口；客户端可做本地预检和友好提示，但以服务端结果为准。

**Q6：暗子和明子走法有何不同？**  
**A**：暗子未翻开时按 `virtualType`（该格原始角色）走子；明子按真实 `type`。明士可出九宫、明象可过河是本组强化规则；暗士暗象仍受限。

---

## AI 类

**Q7：AI 会不会透视对手暗子？**  
**A**：不会。搜索前调用 `createAiPublicView`，对手未翻开子 `type=UNKNOWN`；Hard 档用 Belief Sampling 在合法采样空间内推断，而非读取真实 type。

**Q8：三档 AI 本质区别？**  
**A**：Easy 启发式+随机；Medium 单局面 Alpha-Beta 迭代加深；Hard 对对手暗子多次采样后分别搜索，取期望最优步。

**Q9：Alpha-Beta 怎么剪枝？**  
**A**：维护 α/β 界，子节点返回值导致 α≥β 时剪枝；配合置换表、杀手走法、历史启发、LMR 与静态搜索提高效率。

**Q10：置换表如何避免无效命中？**  
**A**：Zobrist 哈希局面 + 深度/边界类型校验；采样场景下可能存在污染，Hard 档每采样独立搜索引擎实例以降低风险。

---

## 工程类

**Q11：为什么选 Maven 多模块？**  
**A**：依赖边界清晰、可单独测试与打包；`jieqi-app` 打 Fat JAR 便于验收机一键运行。

**Q12：怎么保证 AI 不超时？**  
**A**：`timeLimitMs` 截止检查 + 迭代加深保留上一深度最优解 + 超时 fallback 到合法走法。

**Q13：怎么做到一键构建与自检？**  
**A**：`scripts/verify.ps1` 串联 `mvn test`、全模块 compile、Fat JAR package；答辩可先跑自检证明绿。

---

## 产品与协议类

**Q14：跟普通象棋课设有什么不同？**  
**A**：揭棋暗子与翻子随机、信息差复盘；三档 AI 含非完全信息处理；协议对齐课程 v3.0 并标注本组扩展（复盘、rematch）。

**Q15：后续怎么扩展？**  
**A**：Web 前端旁观、走子错误码细化、Redis 房间、GUI；协议扩展已预留 messageType，与老师公共接口分离。

**Q16：复盘为什么不用棋谱重放？**  
**A**：翻子由服务器随机决定，重放文本无法复现相同中间局面；故保存 `ReplayFrame` 棋盘快照与 `replay.json`。

**Q17：哪些消息是你们自己加的？**  
**A**：`replayRequest`/`replayFrame`、`rematchRequest`、`addTime`、`pauseGame` 等；必做互操作仍是 Login/match/move/gameOver 等，见 INTERFACE.typ 对照表。

---

*建议每人熟读与自己负责模块相关的 5 题 · v1.0 · 2026-06-18*

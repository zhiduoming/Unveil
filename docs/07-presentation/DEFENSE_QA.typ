#import "../template.typ": *
#show: doc => [ #cover(title: "答辩问答预演", subtitle: "15+ 高频问题与推荐回答", doc-type: "答辩材料") #doc ]
#setup-doc(title: "Unveil — 答辩问答预演")

= 架构类

*Q1：为什么分 5 个模块？*
A：领域（core）、网络（server/client）、算法（ai）、入口（app）分离。core 不依赖任何外部模块，被所有层复用；server 和 client 只处理通信，不改规则；ai 独立演进，算法升级不影响对局稳定性。

*Q2：某个模块挂了会影响其他吗？*
A：不会。core 有完整单测保障；server 挂了客户端连不上但不影响本地人机（只依赖 core + ai）；ai 挂了不影响双人对弈。

*Q3：为什么用 WebSocket 而非 HTTP？*
A：象棋对弈需要全双工推送（对手走子、超时、聊天），HTTP 轮询延迟高。WebSocket 一次握手后双向推送，适合实时博弈场景。

= 规则类

*Q4：如何保证规则校验正确？*
A：12 项自动化测试覆盖七种走法、暗子约束、送将/照面拒止、将死/困毙判定。校验在服务端执行，客户端不走终局逻辑。

*Q5：规则校验在客户端还是服务器？*
A：服务器是权威。客户端可本地预检（纯 UI 优化），但终局判定、翻子随机、超时全由服务器决定。

*Q6：暗子和明子走法上有何不同？*
A：暗子按 virtualType（原位角色）走子，明子按真实 type 走子。暗士限九宫、暗象不过河；翻开后明士可出九宫、明象可过河（强化规则）。

= AI 类

*Q7：AI 是否透视对手暗子？*
A：不透视。AI 调用 `createAiPublicView` 将对手暗子 type 置 UNKNOWN。Hard 档用 Belief Sampling 对对手暗子身份多次采样后求期望。

*Q8：三档 AI 的本质区别？*
A：Easy 不搜索，纯启发+随机；Medium 在公开视角上 Alpha-Beta 搜索；Hard 对外采样的确定化局面上做 Alpha-Beta 后求期望。

*Q9：Alpha-Beta 怎么剪枝？*
A：维护 [alpha, beta] 窗口。某分支得分 ≤ alpha（己方已有更好）或 ≥ beta（对方可强反击）时剪枝。结合迭代加深、置换表、杀手/历史启发加速。

*Q10：置换表怎么避免哈希冲突？*
A：64 位 Zobrist 哈希。冲突概率 ≈ 1/2^64，搜索树中可接受。TT 条目含 depth/flag/score 三重校验。

= 工程类

*Q11：为什么选 Maven 多模块？*
A：编译隔离（改 ai 不重编 core）、依赖单向（core → server/client/ai → app）、Fat JAR 打包简便。

*Q12：怎么保证 AI 不超时？*
A：每层搜索前检查 `System.currentTimeMillis() < deadline`。超时立即截断，返回已完成层最优走法。最坏情况下 fallback 到合法随机着。

*Q13：一键构建 + 一键自检怎么实现？*
A：`scripts/verify.ps1`：mvn test → mvn compile → mvn package，三步全过输出 "OK: verify passed"。

= 产品类

*Q14：跟别人棋类项目有什么不同？*
A：揭棋规则（暗子/翻子/强化士象）+ 服务端权威校验 + 非完全信息 AI（Belief Sampling）+ 课程公共接口对齐 + Maven 多模块 + 复盘快照时间线。

*Q15：后续可以怎么扩展？*
A：v1.1 错误码细化；v2.0 Web GUI 旁观、排行榜；多组联调互操作矩阵。

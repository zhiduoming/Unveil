#import "../template.typ": *
#show: doc => [ #cover(title: "产品需求文档", subtitle: "PRD — 产品定位、用户角色、版本规划", doc-type: "产品体验") #doc ]
#setup-doc(title: "Unveil — 产品需求文档（PRD）")

= 产品定位

*一句话*：面向课程验收的揭棋对弈系统，重点展示面向对象设计、网络通信、规则校验、AI 博弈、棋谱复盘和工程化自检。

*优先级排序*（产品决策原则）：

```
规则稳定 > AI 可解释 > 复盘可追溯 > 一键运行 > 演示流程清晰 > 花哨 GUI
```

*不追求*：商业级棋牌 App 体验、用户增长、付费体系。
*追求*：老师 3 分钟看懂、8 分钟演示不翻车、同学可按公共接口互联。

= 用户角色与场景

#table(
  columns: (auto, auto, auto),
  [*角色*], [*典型场景*], [*使用入口*],
  [普通玩家], [课后与同学联网下一盘揭棋], [client-ws 控制台客户端],
  [AI 挑战者], [单人练习，选择 Easy/Medium/Hard 人机对战], [交互菜单「人机对战」],
  [教师验收者], [检查协议、规则、AI、文档是否达标], [verify.ps1 + DEMO_SCRIPT.typ + 双客户端],
  [观战者（扩展）], [旁观他人对局学习], [协议预留，实验性],
)

= 用户痛点

#table(
  columns: (auto, auto, auto),
  [*痛点*], [*现状*], [*Unveil 解决方案*],
  [规则判断困难], [暗子/翻子/强化士象规则复杂，人工易错], [服务端 RuleValidator 权威校验],
  [网络状态不同步], [自行 socket 易棋盘不一致], [WebSocket JSON 广播 moveResult],
  [AI 作弊疑虑], [简单 AI 可能透视暗子], [createAiPublicView + AiFairnessTest],
  [无法复盘], [棋谱重放因翻子随机无法还原], [ReplayTimeline 逐步棋盘快照],
  [验收不可复现], [环境杂乱、无自检], [Maven 多模块 + verify.ps1 + 演示脚本],
  [组间无法互联], [各组协议不一致], [INTERFACE.typ v3.0 权威协议 + PDF],
)

= 核心功能（用户视角）

== 对弈

- 登录服务器，输入 userId 与密码
- 匹配对手或指定 AI 对战
- 控制台棋盘显示（红方在下、黑方在上）
- 输入坐标走子（如 `move e6 e5`）
- 实时看到对方走子、翻子结果、吃子
- 聊天、提和、认输、请求加时

== 规则保障

- 非法走子立即提示，棋盘不变
- 超时自动判负（65 秒）
- 终局自动判定：将死、困毙、和棋、长将长捉等
- 终局弹窗式摘要：胜者、原因、总步数

== AI 对手

- 三档难度：入门（Easy）、进阶（Medium）、挑战（Hard）
- AI 思考有进度感（控制台输出思考时间）
- 不透视对手暗子，符合揭棋信息差
- 可在同一服务器与人类混排

== 棋谱与复盘

- 每局自动生成文字棋谱（records/\*.jieqi）
- 终局后可「上一步 / 下一步」复盘
- 复盘显示当时棋盘（终局后可上帝视角）
- 支持导出路径提示

== 工程体验

- 一个 Fat JAR 或 `mvn exec:java` 启动菜单
- 9 项菜单：服务器、客户端、AI、自检等
- Docker Compose 可选一键起服务

= 版本规划

== v1.0 — 验收版（当前目标）

#table(
  columns: (auto, auto),
  [*能力*], [*说明*],
  [WebSocket 双人对弈], [端口 8887，对齐课程公共接口],
  [完整揭棋规则引擎], [含暗子、翻子、强化士象、长将长捉],
  [三档 AI], [Easy / Medium / Hard],
  [棋谱 + 内存复盘 + JSON 落盘], [ReplayTimeline],
  [Maven 5 模块 + 自检脚本], [verify.ps1],
  [文档体系 24 份], [docs/README.typ 导航],
)

== v1.1 — 增强版（答辩后）

- 错误原因码：RuleValidator 返回具体拒绝原因
- Hard AI 调优：belief sampling 深度与采样次数优化
- 长捉边界测试补全
- JaCoCo 覆盖率报告

== v2.0 — 平台版（远期）

- Web 棋盘前端（Vue/React 旁观与对战）
- 断线重连
- Redis 房间多实例部署
- 战绩统计 / 排行榜

= 成功指标

#table(
  columns: (auto, auto, auto),
  [*指标*], [*目标值*], [*测量方式*],
  [单元测试通过率], [100%], [mvn test],
  [验收项 C1–C20 通过], [≥ 18/20], [ACCEPTANCE_CRITERIA.typ 勾选],
  [演示脚本时长], [6–8 分钟], [DEMO_SCRIPT.typ 彩排计时],
  [AI Easy 步时], [< 500ms], [PerformanceTest],
  [AI Medium/Hard 步时], [< 60s], [实战日志],
  [非法走子拒绝率], [100%], [非法走法测试集],
  [协议字段一致性], [与 INTERFACE.typ 零矛盾], [人工 + 互操作测试],
)

= 非目标（明确不做）

- 商业运营、支付、账号体系
- 移动端 App
- 实时语音
- 反作弊 beyond 服务端校验
- 图形化 3D 棋盘

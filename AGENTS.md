# Unveil — 揭棋对弈程序设计

第一组（组长 张恒基）课程大作业。基于 TCP Socket 的揭棋（Hidden Chess）客户端-服务器对弈系统。

## 项目结构

```
Unveil/
├── jieqi-core/       # 领域、规则、协议（Board, Game, EndgameJudge, RuleValidator）
│   └── com.jieqi.record/   # 棋谱记法与导入（GameRecord, MoveNotation）
├── jieqi-server/     # TCP 服务器（GameServer, ClientHandler, GameRecordStore）
├── jieqi-client/     # TCP 客户端（GameClient, ConsoleUI）
├── jieqi-ai/         # AI 博弈（JieqiAgent, Alpha-Beta, 期望值评估）
│   └── com.jieqi.ai.agent/ # 多 Agent 编排（Probability / Endgame / Search）
├── jieqi-app/        # 可选 GUI 启动器
├── docs/             # 接口文档与实验报告
│   ├── INTERFACE.typ # 权威协议规范（Typst，编译为 PDF）
│   └── INTERFACE.pdf
└── pom.xml           # Maven 父 POM（多模块）
```

## 构建与运行

```bash
# 编译全部模块
mvn clean compile -f pom.xml

# 启动服务器（默认端口 8888）
mvn exec:java -pl jieqi-server -Dexec.mainClass="com.jieqi.server.GameServer"

# 启动客户端
mvn exec:java -pl jieqi-client -Dexec.mainClass="com.jieqi.client.GameClient"

# 编译协议文档为 PDF
typst compile docs/INTERFACE.typ docs/INTERFACE.pdf
```

## 协议文档

- 组间互操作以 `docs/INTERFACE.typ` 为唯一权威格式。
- 每次修改协议必须先更新 Typst 文档，再改代码。
- 消息帧格式：`msgType|payloadByteLength|payload\n`，UTF-8 编码，LF 行尾。
- 完整消息类型、错误码、原因码定义见 Protocol.java 及文档 §3。

## 代码约定

- JDK **21**（与 `README.md`、CI 一致），源码编码 UTF-8
- 包结构：`com.jieqi.core`（领域）、`com.jieqi.protocol`（协议）、`com.jieqi.record`（棋谱）、`com.jieqi.server`、`com.jieqi.client`、`com.jieqi.ai`、`com.jieqi.ai.agent`
- 坐标系统：行号 9（顶/黑方）→ 0（底/红方），列号 a（左）→ i（右）
  - 内部数组：`row = 9 - displayRow`，`col = coord.charAt(0) - 'a'`
  - BOARD_STATE 序列化：row0 = 顶行（黑方），row9 = 底行（红方）
- 时间戳以服务器为准，客户端时间戳不被信任
- 类型编码：0=将/帅, 1=车, 2=马, 3=炮, 4=兵/卒, 5=士/仕, 6=象/相

## AI 约束

- 修改 `jieqi-core` 的领域类时必须同步审查 RuleValidator 的规则正确性。
- 协议层改动必须与 `docs/INTERFACE.typ` 保持一致，严禁代码与文档脱节。
- 新增消息类型必须同步更新 Protocol.java、ClientHandler.java、GameClient.java 三处。

## 开发模式与 AI 协作约束

本项目的开发严格遵守"人机协作、逐层细化"原则：

1. **架构先行**：任何代码生成前，必须确认已在 `docs/` 中定义好对应的接口与数据模型。禁止在未明确 INTERFACE.typ 规范的情况下编写通信逻辑。
2. **任务颗粒度**：开发任务需拆解为 200-500 行代码的可独立验证单元。每次对话仅专注于一个模块。
3. **测试驱动**：优先生成单元测试，确保逻辑正确性，再生成实现代码。
4. **接口先行**：任何改动先更新 `docs/INTERFACE.typ`，确保各模块接口对齐。

## Git 提交规范

每完成一个功能点或修复一个 Bug，必须自动提交。提交信息严格遵循格式：

```
type: 简要描述
```

`type` 取值范围（**仅**使用以下五种，勿用 `chore` / `ci` 等）：
- `feat`：新增功能
- `fix`：修复 Bug
- `docs`：文档/协议规范更新
- `refactor`：代码重构
- `test`：测试用例

示例：
```
feat: init jieqi-core domain classes
fix: RuleValidator Advisor dark-piece palace restriction
docs: sync INTERFACE.md with protocol v2 specification
```

## 当前工作流

1. **需求分析**：先通过 `docs/` 整理需求，确认边界条件。
2. **接口先行**：任何改动先更新 `docs/INTERFACE.typ`，确保各模块接口对齐。
3. **编码实现**：编写对应的 Java 实现类。
4. **测试验证**：运行测试用例，集成调试。
5. **规范提交**：执行 `git commit -m "type: message"`。

## AI 博弈引擎约束

- 必须实现 Alpha-Beta 剪枝算法。
- 局面评估函数必须包含对"暗子"的概率期望值（Expectimax 思想）。
- 确保 AI 决策在规定时间内（每步约 1 分钟，考虑网络延时）完成。
- Board 类需提供局面哈希方法，供长将/长捉重复局面判定使用。

---
*张恒基（Bosprimigenious）团队项目*

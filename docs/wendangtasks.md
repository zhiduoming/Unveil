# 揭棋文档体系重建 — 落地级实施任务书

> 基于张恒基 (Bosprimigenious) 对全仓库文档现状的走读分析，交付给执行者直接编写。
> 目标：从"散落 Markdown"重构成"层次清晰、老师说不出毛病"的六类文档体系。

---

## 〇、现状诊断

### 当前 docs/ 下有什么

```
docs/
├── ARCHITECTURE.md              ← 已有，但缺少领域模型、数据结构设计
├── REQUIREMENTS.md              ← 已有，但状态全标"已实现"，缺分级
├── REPORT.md                    ← 极简（3KB），不可直接交
├── TEAM.md                      ← 仅有分工表，缺成员贡献详情
├── INTERFACE.typ                ← 核心文档，已有 v3.0
├── INTERFACE.md / INTERFACE.pdf ← 衍生品
├── INTEROP.md                   ← 仅 2KB，太简单
├── TASKS.md                     ← 1.5KB，无跟踪价值
├── COMPLETION_REPORT.md         ← 流水账
├── CODEBASE_AND_TEACHER_MIGRATION_PLAN.md  ← 过渡产物，不可交
├── CURSOR_TASKS.md              ← 开发过程产物，不可交
├── MANUAL_TESTING_*.md (x2)    ← 测试记录，格式太原始
├── TEACHER_WS_PROTOCOL.md       ← 与 INTERFACE.typ 重复
├── fupantasks.md                ← 本次新建，实现指南
├── suanfatasks.md               ← 本次新建，实现指南
└── chanpintasks.md              ← 本次新建，实现指南
```

**核心问题**：
1. 没有分类目录，全平铺
2. 多个文档是开发过程产物（CURSOR_TASKS、CODEBASE_MIGRATION），不能交
3. REQUIREMENTS.md 状态全是"已实现"，缺乏可信度
4. 缺规则引擎设计、AI 算法设计、复盘设计、测试报告、答辩材料
5. REPORT.md 太薄，没有答辩价值

---

## 一、目标文档目录

```
docs/
├── 00-overview/                       # 验收报告类
│   ├── PROJECT_OVERVIEW.md            # 项目总览（3 分钟看懂）
│   ├── FEATURE_MATRIX.md              # 功能完成度矩阵
│   └── GLOSSARY.md                    # 术语表
│
├── 01-requirements/                   # 需求分析类
│   ├── REQUIREMENTS.md                # 需求追踪（已有，需分级重写）
│   └── ACCEPTANCE_CRITERIA.md         # 验收标准对照
│
├── 02-design/                         # 技术设计类
│   ├── ARCHITECTURE.md                # 总体架构（已有，需深化）
│   ├── DOMAIN_MODEL.md                # 领域模型（新增★）
│   ├── RULE_ENGINE_DESIGN.md          # 规则引擎设计（新增★）
│   ├── AI_DESIGN.md                   # AI 算法设计（新增★）
│   └── REPLAY_DESIGN.md               # 复盘设计（新增）
│
├── 03-interface/                      # 接口协议类
│   ├── INTERFACE.typ                  # 权威协议（已有）
│   ├── INTERFACE.pdf                  # 编译产物（已有）
│   └── MESSAGE_EXAMPLES.md            # 消息交互示例（新增）
│
├── 04-deployment/                     # 工程交付类
│   ├── BUILD_AND_RUN.md               # 构建与运行（新增★）
│   ├── DOCKER_DEPLOYMENT.md           # Docker 部署（新增）
│   └── TROUBLESHOOTING.md             # 常见问题排查（新增）
│
├── 05-testing/                        # 测试证明类
│   ├── TEST_PLAN.md                   # 测试方案（新增）
│   ├── TEST_CASES.md                  # 测试用例清单（新增★）
│   └── TEST_REPORT.md                 # 测试报告（新增★）
│
├── 06-product/                        # 产品体验类
│   ├── PRODUCT_REQUIREMENTS.md        # 产品需求 PRD（新增）
│   ├── USER_JOURNEY.md                # 用户旅程（新增）
│   └── COMPETITOR_ANALYSIS.md         # 竞品分析（新增）
│
├── 07-presentation/                   # 答辩材料类
│   ├── DEMO_SCRIPT.md                 # 演示脚本（新增★）
│   ├── DEFENSE_QA.md                  # 答辩问答预演（新增★）
│   └── FINAL_REPORT.md                # 最终大报告（新增★）
│
├── fupantasks.md                      # [实现指南，不入库]
├── suanfatasks.md                     # [实现指南，不入库]
├── chanpintasks.md                    # [实现指南，不入库]
└── TEAM.md                            # [保留]
```

> 标 ★ 的是最优先写的 10 份。标 [实现指南] 的三个 tasks 文件不交老师，只供内部开发用。

---

## 二、逐文档详细要求

---

### 【00-overview】验收报告类

#### 1. PROJECT_OVERVIEW.md — 项目总览

**目标读者**：老师（3 分钟了解全貌）

**必须包含的章节**：

| 章节 | 内容要求 | 长度 |
|---|---|---|
| 项目名称与代号 | 揭棋对弈程序 Unveil | 1 行 |
| 项目背景 | 揭棋规则复杂（暗子/翻子/强化士象），需要系统自动校验、网络对弈、AI 对手、复盘追溯 | 3-5 句 |
| 核心功能（列表） | 揭棋规则引擎、WebSocket 真人对弈、TCP 兼容、三档 AI、棋谱与复盘、Maven 多模块 | 6 条 |
| 技术栈 | Java 21, Maven, Java-WebSocket, Gson, JUnit 5 | 一行 |
| 项目规模 | 模块数 × 类数 × 代码行数（用 cloc 统计） | 3 个数字 |
| 项目结构 | 一张 ASCII 树形图（5 模块 + docs） | 10 行 |
| 演示入口 | 一行命令 + 一张截图说明 | 2 行 |
| 与课程要求的对应 | 表格：课程要求 → 本项目实现位置 | 8 行 |

**写法要求**：
- 每段不超过 5 句
- 用表格和列表，不用大段文字
- 不要出现"我们觉得""我认为"
- 代码行数必须用 `cloc` 真实统计，不许估

**与其他文档的链接**：顶部放一栏导航，链接到其他核心文档

---

#### 2. FEATURE_MATRIX.md — 功能完成度矩阵

**目标读者**：老师（一眼看懂"做了什么、做到什么程度"）

**四档状态定义**：

| 状态 | 标签 | 含义 |
|---|---|---|
| ✅ 已实现 | 代码稳定，能演示，有测试 | 可直接验收 |
| ⚡ 已实现-待强化 | 能跑，但边界测试待补或参数待调优 | 基础功能 OK |
| 🔬 实验性扩展 | 有雏形，不作为主验收承诺 | 加分项 |
| 📋 规划中 | 有设计，代码未开始 | 不予验收 |

**表格格式**（核心）：

```
模块 | 功能 | 实现位置 | 状态 | 备注
```

**必须覆盖的模块**：

1. 规则引擎（15+ 行：走子规则、暗子翻子、禁送将、将帅照面、终局判定、长将长捉等）
2. 网络对弈（10+ 行：WebSocket/TCP、匹配、房间、计时、全局广播等）
3. AI 博弈（8+ 行：三档、搜索算法、评估函数、暗子处理等）
4. 棋谱与复盘（6+ 行：文字棋谱、棋盘快照、内存复盘、文件落盘等）
5. 客户端（6+ 行：交互菜单、棋盘显示、命令解析、复盘操作等）
6. 工程化（6+ 行：Maven 多模块、Docker、自检脚本、演示脚本等）

**写法要求**：
- 状态标注务求保守。不确定的标 ⚡，不要标 ✅
- 备注栏写清限制条件。例如长将应该写"已实现 6 次判负，但长捉分类仍需人工复核"
- 不要出现"完美""完全""全部"

---

#### 3. GLOSSARY.md — 术语表

**内容**：项目中所有专有名词的统一定义。

| 术语 | 英文 | 定义 | 代码对应 |
|---|---|---|---|
| 揭棋 | Hidden Chess / JieQi | 中国象棋变体，开局仅将帅明，其余随机暗置 | — |
| 暗子 | Dark / Hidden Piece | 未翻开的棋子，按所在位置的原始象棋角色走子 | `ChessPiece.revealed=false` |
| 明子 | Revealed Piece | 已翻开的棋子，按真实身份走子 | `ChessPiece.revealed=true` |
| 翻子 | Flip / Reveal | 暗子首次移动或吃子后公开真实身份 | `executeMove()` |
| 虚拟类型 | Virtual Type | 暗子按所在位置的角色（如原位是马则按马走） | `ChessPiece.virtualType` |
| 上帝视角 | God View | 能看所有棋子真实身份（复盘/AI 自对弈） | — |
| ... | ... | ... | ... |

**必须收录的术语**：暗子、明子、翻子、虚拟类型、上帝视角、AI 公开视角、棋谱文本、复盘帧、快照、belief sampling、置换表、迭代加深、静态搜索、SEE

**写法要求**：每行定义 1-2 句话，不要展开

---

### 【01-requirements】需求分析类

#### 4. REQUIREMENTS.md — 需求追踪（重写）

**现状**：已有，但状态全写"已实现"

**重写要求**：

1. **功能需求替换状态标签**：把每个"已实现"替换为上文的四档标签
2. **增加用户角色章节**：普通玩家、AI 对手、观战者、教师验收者（各 2-3 句描述）
3. **增加非功能需求表格**：正确性、响应时间、可测试性、可扩展性、可部署性
4. **5 节需求优先级重新排序**：P0 必须 / P1 重要 / P2 扩展
5. **边界与不做项补充**：把不应现在做的也列清（GUI、伪造时间戳、Web 前端、排行榜）

**关键改写点**（逐条过）：

| 原标注 | 应改为 | 原因 |
|---|---|---|
| F2 "已实现 RuleValidator（双端均调用）" | ⚡ 已实现-待强化 | RuleValidator 返回 boolean，未区分错误原因 |
| F3 "已实现 isValidMove + isMoveLegal" | ✅ 已实现 | 核心正确 |
| F7 "已实现 WsGameServer / Game" | ✅ 已实现 | 65s 超时稳定 |
| 暗子士象 "已实现" | ✅ 已实现 | 有测试覆盖 |
| 长将长捉 "已实现" | ⚡ 已实现-待强化 | 需要完整边界测试 |
| 复盘 "—" | 📋 规划中 | 或实现后改 ✅ |

---

#### 5. ACCEPTANCE_CRITERIA.md — 验收标准对照

**格式**：对号表。

| 编号 | 课程要求 | 本项目对应 | 验收方式 | 状态 |
|---|---|---|---|
| C1 | 网络揭棋双人对弈 | WebSocket 8887 端口 | 启动服务器 + 2 客户端对弈 | ✅ |
| C2 | 服务器校验非法着法 | RuleValidator + Game.processMove | 客户端发非法走法，观察拒绝 | ✅ |
| ... | ... | ... | ... | ... |

**必须覆盖的课程要求**：
- 网络对弈
- 规则校验（至少 5 条子规则）
- 棋谱记录
- 翻子随机
- 超时
- AI 博弈
- 面向对象设计
- 文档与互操作

**写法要求**：验收方式必须可操作（"启动服务器、客户端 1 发 move a1 a1，观察返回 error"）

---

### 【02-design】技术设计类

#### 6. ARCHITECTURE.md — 总体架构（深化）

**现状**：有初版，需深化

**必须增加的内容**：

1. **Mermaid 架构图强化**：当前有 flowchart TB，需要增加：
   - 模块依赖关系图（箭头方向）
   - 对局主流程时序图（Client → Server → Game → Board → RuleValidator → EndgameJudge）
   - AI 调用流程图（AgentOrchestrator → ProbabilityAgent / EndgameAgent / SearchAgent → OptimizedAlphaBeta → EnhancedEvaluator）

2. **模块职责表**：

| 模块 | 职责 | 不做什么 | 包结构 |
|---|---|---|---|
| jieqi-core | 棋盘、棋子、规则、终局、棋谱、协议 | 不含网络、AI、UI | com.jieqi.core/record/protocol |
| jieqi-server | WebSocket/TCP 服务、房间、匹配、持久化 | 不含规则判断 | com.jieqi.server/ws |
| jieqi-client | 控制台交互、棋盘显示 | 不含规则判断 | com.jieqi.client |
| jieqi-ai | 搜索、评估、Agent 编排 | 不含领域逻辑 | com.jieqi.ai/bot/agent/belief |
| jieqi-app | 统一启动菜单、CLI 参数 | 无新业务逻辑 | com.jieqi.app |

3. **核心类图**（Mermaid ClassDiagram）：
   - Board ← Game → GameRecord
   - Game → EndgameJudge
   - Game → RuleValidator
   - AiBot ← EasyRuleBot / AlphaBetaBot / BeliefAlphaBetaBot

4. **数据流**：一页讲清 moveRequest → processMove → executeMove → record append → replayFrame

---

#### 7. DOMAIN_MODEL.md — 领域模型（新增★）

**为什么必须有**：面向对象课程，老师要看你的类设计。

**内容**：

1. **核心领域类表**：

| 类 | 包 | 职责 | 关键字段 |
|---|---|---|---|
| Board | core | 10×9 棋盘，棋子查询，move/undo | grid[10][9], pieces list, moveHistory |
| ChessPiece | core | 棋子状态 | color, type, virtualType, revealed, row, col |
| Move | core | 走法数据 | source, destination, isFlipOnly, type |
| Game | core | 对局状态机 | board, currentTurn, status, record, replayTimeline |
| GameRecord | record | 文字棋谱 | lines: List<String> |
| GameRecordStore | server | 棋谱落盘 | save to records/*.jieqi |
| ReplayTimeline | record | 复盘时间线 | frames: List<ReplayFrame> |
| ReplayFrame | record | 复盘单帧 | stepIndex, move, boardSnapshot |

2. **类图**（Mermaid）：Board 1←→* ChessPiece, Game 1→1 Board, Game 1→1 GameRecord, Game 1→1 ReplayTimeline

3. **状态机**（GameStatus 转换图）：WAITING → PLAYING → RED_WIN / BLACK_WIN / DRAW（附触发条件）

4. **不变式约束**（关键）：
   - 棋盘上红黑双方永远不共享同一个格子
   - currentTurn 只能是 RED 或 BLACK
   - revealed=false 的棋子 type 可能被服务器改动（翻子），但 virtualType 不变
   - noCaptureCount ≤ 80

---

#### 8. RULE_ENGINE_DESIGN.md — 规则引擎设计（新增★）

**为什么必须有**：揭棋最难的是规则。老师会重点看。

**内容要求**：

1. **坐标系统说明**（配 ASCII 图）：
   ```
     a  b  c  d  e  f  g  h  i
   9 -- -- -- -- -- -- -- -- --  黑方底线
   8 -- -- -- -- -- -- -- -- --  
   ...
   0 -- -- -- -- -- -- -- -- --  红方底线
   ```
   内部数组 row=0 对应显示行 9（顶/黑方），row=9 对应显示行 0（底/红方）

2. **棋子模型**（配字段表格 + 示例）：
   - 明子：type=ROOK, virtualType=ROOK, revealed=true
   - 暗子：type=UNKNOWN, virtualType=ROOK（该位原始角色）, revealed=false

3. **七种棋子走法规则**（每种配规则描述 + 特殊边界）：
   - 车/俥：直线移动，无阻隔（与明/暗无关）
   - 马/傌：日字形，蹩马腿（明暗均受约束）
   - 炮/砲：直线移动不吃子，吃子需一个炮架
   - 兵/卒：前进 1，过河可左右 1
   - 将/帅：九宫内前后左右 1
   - 士/仕：斜走 1；明士可出九宫（强化规则）
   - 象/相：田字形，塞象眼；明象可过河（强化规则）

4. **暗子特殊规则**：
   - 暗子走法按 virtualType（位置角色）
   - 暗士/暗象限九宫/不过河（与明士/明象不同）
   - 原地翻子禁止（source ≠ destination）
   - 首次移动后翻开

5. **终局判定**（每种配判定流程/伪代码）：
   - 将死：对方无合法走法 + 正在被将军
   - 困毙：对方无合法走法 + 未被将军
   - 超时：65 秒（60s + 5s 裕量）
   - 认输
   - 40 步无吃子和棋（noCaptureCount ≥ 80，双方各 40 步）
   - 长将判负：连续将军 6 次
   - 长捉判负：连续捉子 6 次（兵卒长捉判和）

6. **校验流程**（ASCII 流程图或 Mermaid）：
   ```
   moveRequest → 对局进行中? → 轮到你? → 超时? → 禁止原地翻子? → isValidMove(棋子规则) → isMoveLegal(不能送将) → executeMove → checkAfterMove(终局判定)
   ```

7. **已知限制**：长捉分类（将/杀/捉）的精确判断需要人工复核

---

#### 9. AI_DESIGN.md — AI 算法设计（新增★）

**为什么必须有**：这是本项目的算法加分核心。

**内容要求**：

1. **设计目标与约束**：
   - 走法必须合法（通过 generateLegalMoves + 最终 fallback）
   - 不能透视对手暗子（仅用 createAiPublicView）
   - 必须按时返回（有超时 fallback）
   - 三档难度可感知

2. **三档 AI 对照表**：

| 维度 | Easy | Medium | Hard |
|---|---|---|---|
| 实现类 | EasyRuleBot | AlphaBetaBot → JieqiAgent | BeliefAlphaBetaBot |
| 搜索算法 | 无（启发式选择） | Alpha-Beta + 置换表 | Belief Sampling + Alpha-Beta |
| 搜索深度 | 0 | 迭代加深到深度 ≥ 8 | 每次搜索深度 4-6 |
| 暗子处理 | 公开视角 | 公开视角 | 对对手暗子采样确定化 |
| 时间预算 | < 500ms | ~5s | ~5s（24 次搜索） |
| 随机性 | 30% 全随机 + 70% TopK | 无（纯搜索） | 采样随机性 |

3. **搜索算法详解**（每种配一段文字说明 + 伪代码）：
   - Alpha-Beta 剪枝：原理 + 在揭棋中的适用性
   - 迭代加深：depth 1→20，逐层加深
   - 置换表：ZobristHash → TT 查询/存储
   - 杀手启发：beta 剪枝时记录杀手走法
   - 历史启发：推高 alpha 时更新历史表
   - 静态搜索：只在吃子中扩展，最多 3 层
   - SEE 静态交换评估：模拟目标格连续交换
   - 长将规避：repetitionRisk 惩罚

4. **评估函数详解**（EnhancedEvaluator 的 7 个维度）：
   - 子力（material）：明子固定值 + 暗子期望值
   - 位置（position）：车马炮兵的位置分表
   - 机动性（mobility）：合法走法数量
   - 将帅安全（kingSafety）：士象护卫 + 宫心控制
   - 威胁（threats）：MVV-LVA 威胁评估
   - 兵形（pawnStructure）：兵卒相邻加成
   - 残局猎杀（kingHunt）：子少时压迫对方将位

5. **揭棋隐藏信息的处理**（重点）：
   - AI 如何不透视：`board.createAiPublicView(color)` — 对手暗子 type 设为 UNKNOWN
   - Hard 的 Belief Sampling：`BoardSampler.fromPublicView()` — 根据已揭示对手棋子，反推剩余子力池，随机分配暗子身份
   - 为什么要采样：揭棋是非完全信息博弈，无法直接确定对手局面
   - 与标准 Alpha-Beta 的关系：对每个采样局面上做 Alpha-Beta，最后期望收益最大的走法

6. **已知限制**：Hard 的 belief sampling 在时间受限时搜索浅；置换表在采样场景下可能缓存污染

---

#### 10. REPLAY_DESIGN.md — 复盘设计（新增）

**内容要求**：

1. **为什么不能只用棋谱重走**（3 点理由）：
   - 揭棋翻子结果由服务器随机决定，重走可能得到不同翻子结果
   - 暗子真实身份在服务器端，客户端只知公开信息
   - 单纯 move 序列无法稳定还原所有中间棋盘状态

2. **数据结构**：
   - ReplayTimeline：List\<ReplayFrame\>
   - ReplayFrame：stepIndex / move / boardSnapshot / currentTurn / status / timestamp / captured

3. **帧编号模型**：stepIndex=0 开局，stepIndex=n 终局

4. **存储策略**：
   - 内存：Game.replayTimeline（运行时，对局结束、房间清理前可用）
   - 文件：records/\<gameId\>.replay.json（持久化，房间销毁后可用）

5. **产生时机**：
   - startGame → recordInitial（第 0 帧）
   - processMove 成功 → recordAfterMove（每步后一帧）

6. **协议**（表格）：
   - replayRequest（C→S）：{ messageType, stepIndex? }
   - replayFrame（S→C）：{ messageType, roomId, stepIndex, totalSteps, currentTurn, status, move?, captured?, board }

7. **复盘操作的权限与信息差**：
   - 对局中：按玩家视角显示（对手暗子不可见真实身份）
   - 终局后：可用上帝视角（所有棋子真实身份可见）
   - 仅房间内玩家可请求复盘

---

### 【03-interface】接口协议类

#### 11. MESSAGE_EXAMPLES.md — 消息交互示例（新增）

**内容**：每个 messageType 的一个完整 JSON 示例 + 说明。不要纯描述协议格式，而是给出能直接看懂的交互实例。

**必须收录的消息类型**：

| messageType | 方向 | 示例 | 说明 |
|---|---|---|---|
| login | C→S | `{"messageType":"login","userId":"player1","passwordHash":"..."}` | |
| loginResponse | S→C | `{"messageType":"loginResponse","success":true}` | |
| match | C→S | `{"messageType":"match"}` | |
| matchSuccess | S→C | `{"messageType":"matchSuccess","roomId":"room_xxx","yourColor":"red","opponentId":"player2"}` | |
| ready | C→S | `{"messageType":"ready"}` | |
| first | C→S | `{"messageType":"first","firstHand":true}` | |
| gameStart | S→C | 含 initialBoard 的完整 JSON | 配一个简化的 3×3 棋盘示例 |
| move | C→S | `{"messageType":"move","fromX":"a","fromY":6,"toX":"a","toY":5}` | |
| moveResult | S→C | 含 flipResult 的完整 JSON | |
| gameOver | S→C | `{"messageType":"gameOver","winner":"red","reason":"CHECKMATE"}` | |
| chat | 双向 | | |
| drawOffer / drawResponse | 双向 | | |
| resign | C→S | | |
| rematchRequest / rematchResponse | 双向 | | |
| replayRequest | C→S | | |
| replayFrame | S→C | 含 board 的完整 JSON | |

**写法要求**：每个示例必须是可以直接发给服务器的合法 JSON，不能是伪代码

---

### 【04-deployment】工程交付类

#### 12. BUILD_AND_RUN.md — 构建与运行（新增★）

**内容**：

1. **环境要求**：
   - JDK 21（检查 `java -version`）
   - Maven 3.9+（检查 `mvn -version`）
   - Windows / macOS / Linux 均可

2. **构建**（一行命令 + 预期输出）：
   ```bash
   mvn clean package -DskipTests
   ```
   预期输出：BUILD SUCCESS，5 个模块全部编译

3. **运行所有模式**（一个表格）：

| 模式 | 命令 | 预期行为 |
|---|---|---|
| 交互菜单 | `mvn exec:java -f jieqi-app/pom.xml -am` | 显示 1-9 菜单 |
| WebSocket 服务器 | `... -Dexec.args="server-ws 8887"` | 监听 8887 端口 |
| WebSocket 客户端 | `... -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"` | 连接，显示棋盘 |
| AI 自动对弈 | `... -Dexec.args="ai-ws ws://127.0.0.1:8887 ai_bot_1 pw123"` | 自动走子 |
| TCP 服务器 | `... -Dexec.args="server 8888"` | 监听 8888 端口 |
| TCP 客户端 | `... -Dexec.args="client 127.0.0.1 8888 player1 123456"` | 连接 |

4. **完整演示流程**（一步步写清操作 + 预期输出）：
   ```
   终端 1: mvn exec:java ... server-ws 8887
   终端 2: mvn exec:java ... client-ws ws://127.0.0.1:8887 player1 123456
   > login
   > match
   ...
   ```

5. **自检**：`powershell -File scripts/verify.ps1`

---

#### 13. DOCKER_DEPLOYMENT.md — Docker 部署（新增）

**内容**：
1. 前置：Docker + Docker Compose
2. 构建镜像
3. docker-compose 启动
4. 端口映射说明（8887:8887, 8888:8888）
5. 日志查看
6. 停止与清理

---

#### 14. TROUBLESHOOTING.md — 常见问题排查（新增）

**内容**：表格格式

| 问题 | 原因 | 解决 |
|---|---|---|
| 端口被占用 `Address already in use` | 上次服务器未关闭 | `netstat -ano \| findstr 8887` 找到 PID 并 kill |
| 客户端连接失败 | 服务器未启动或地址错 | 检查服务器是否在运行，确认 ws:// 不是 http:// |
| Maven 找不到 JDK 21 | JAVA_HOME 未设置或指向旧版本 | `echo %JAVA_HOME%` 检查 |
| AI 超时 | 预算太紧或搜索太深 | 降低 AILevel 或增加 timeLimitMs |
| 棋盘不同步 | 客户端未正确处理 moveResult | 检查 board update 逻辑 |
| verify.ps1 失败 | 测试未通过 | 运行 `mvn test` 查看具体失败用例 |
| 编译报 `Unsupported class file` | JDK 版本不匹配 | 确认 `java -version` 显示 21 |

---

### 【05-testing】测试证明类

#### 15. TEST_PLAN.md — 测试方案（新增）

**内容**：
1. 测试层次：单元测试（JUnit 5）→ 集成测试 → 手动演示
2. 测试范围：规则、网络、AI、棋谱、复盘
3. 测试环境：JDK 21, Maven 3.9+, Windows
4. 测试覆盖率目标
5. 自动化测试运行命令（`mvn test` / `verify.ps1`）

---

#### 16. TEST_CASES.md — 测试用例清单（新增★）

**格式**：表格

| 编号 | 模块 | 用例名称 | 输入/操作 | 预期结果 | 实现位置 |
|---|---|---|---|---|---|

**最少 40 个用例**，分成 6 组：

**规则测试（15 个）**：
- R01-R07：7 种棋子合法走法各 1 个
- R08-R10：蹩马腿、塞象眼、炮架
- R11：送将拒绝
- R12：将帅照面拒绝
- R13：被将必须解将
- R14：将死判定
- R15：困毙判定

**终局测试（6 个）**：
- E01：40 步无吃子和
- E02：长将判负
- E03：长捉判负
- E04：兵卒长捉和
- E05：超时
- E06：认输

**AI 测试（6 个）**：
- A01：Easy 返回合法走法
- A02：Medium 5 秒返回
- A03：Hard 不透视对手暗子
- A04：AI 被将必须解将
- A05：AI 无合法走法时正确判负
- A06：搜索前后棋盘状态一致

**网络测试（5 个）**：
- N01：双客户端匹配开局
- N02：走子双方同步
- N03：非法走法被拒
- N04：聊天
- N05：提和/认输

**复盘测试（4 个）**：
- P01：开局帧正确
- P02：每步后帧数递增
- P03：上一步/下一步恢复正确
- P04：replay.json 保存

**工程测试（4 个）**：
- B01：`mvn compile` 通过
- B02：`mvn test` 通过
- B03：`verify.ps1` 通过
- B04：`mvn package` 生成可执行 JAR

---

#### 17. TEST_REPORT.md — 测试报告（新增★）

**内容**：

1. **汇总**：总用例数 / 通过 / 失败 / 跳过
2. **逐模块结果表**：每个用例的执行结果（通过/失败）
3. **失败用例详情**：用例编号、失败原因、截图（如有）、是否已修复
4. **AI 性能测试数据**：

| 等级 | 平均步时 | 最小步时 | 最大步时 | 超时次数 | 平均搜索深度 | 平均节点数 |
|---|---|---|---|---|---|---|
| Easy | < 100ms | — | — | 0 | 0 | 0 |
| Medium | ~2s | — | — | 0 | ~10 | ~500K |
| Hard | ~4s | — | — | 0 | ~5 | ~200K |

5. **已知未修问题**：列出，标注优先级

---

### 【06-product】产品体验类

#### 18. PRODUCT_REQUIREMENTS.md — 产品需求 PRD（新增）

**定位**：不是代码需求，而是站在用户/老师/验收方角度的需求文档。

**内容**：
1. 产品定位：面向课程验收的揭棋对弈系统
2. 用户角色与场景（4 类用户）
3. 用户痛点：手动判断规则、网络状态同步困难、AI 透视、缺乏复盘
4. 核心功能（从用户角度描述，不从代码角度）
5. 产品闭环：启动 → 选模式 → 对弈 → 终局摘要 → 复盘 → 再来一局
6. 版本规划：v1.0 验收版 → v1.1 增强版 → v2.0 平台版
7. 成功指标：老师验收通过 + 演示不翻车 + 规则 0 错误

---

#### 19. USER_JOURNEY.md — 用户旅程（新增）

**一幅 ASCII 流程图 + 每个步骤的截图说明**。

流程：

```
进入系统 → 选择模式 → 登录 → 匹配 → 准备 → 开局 → 走子 → 终局 → 查看结果 → 复盘 → 再来一局
```

每个步骤配：操作（用户做什么）、系统行为（系统返回什么）、可选分支。

---

#### 20. COMPETITOR_ANALYSIS.md — 竞品分析（新增）

**三栏对比**：

| 维度 | 普通中国象棋 App | 在线棋类平台 | 简单 Java 棋类课设 | 本项目 Unveil |
|---|---|---|---|---|
| 揭棋规则 | ✗ | ✗ | ✗ | ✓ |
| 网络对弈 | ✓ | ✓ | ✗ | ✓ |
| AI 对手 | ✓ | ✓ | ✗ | ✓ 三档 |
| 规则校验 | 部分 | 强 | 弱 | 强（服务端权威） |
| 复盘 | ✓ | ✓ | ✗ | ✓ |
| 课程接口 | ✗ | ✗ | ✗ | ✓ |
| 多模块工程 | ✗ | ✗ | ✗ | ✓ |

**差异化分析**（3 句话）：不要写"吊打竞品"，要写"课程场景下更合适"

---

### 【07-presentation】答辩材料类

#### 21. DEMO_SCRIPT.md — 演示脚本（新增★）

**格式**：时间线表格

| 时间 | 操作 | 说的话 | 预期结果 |
|---|---|---|---|
| 0:00 | 启动 verify.ps1 | "首先展示自检脚本，所有测试通过" | BUILD SUCCESS |
| 0:30 | 启动交互菜单 | "项目提供统一启动入口" | 显示 1-9 菜单 |
| 1:00 | 启动 WS 服务器 | "启动 WebSocket 服务器，端口 8887" | 监听中 |
| 1:30 | 启动客户端 1 | "第一个玩家登录" | 登录成功 |
| 2:00 | 启动客户端 2 | "第二个玩家登录" | 登录成功 |
| 2:30 | 匹配开局 | "两个玩家匹配，开始对局" | 棋盘显示 |
| 3:00 | 走子展示 | "走一步马" | 走子成功 |
| 3:30 | 非法走法展示 | "发送一个非法走法，系统拒绝" | 非法走法 |
| 4:00 | AI 对战展示 | "切换到人机模式，选择挑战 AI" | AI 思考 |
| 5:00 | 终局展示 | "触发终局条件" | 显示摘要 |
| 5:30 | 复盘展示 | "进入复盘，回看每一步" | 帧切换 |
| 6:00 | 总结 | "项目实现了规则、网络、AI、复盘、工程化的完整闭环" | — |

**总时长控制在 6-8 分钟**。

---

#### 22. DEFENSE_QA.md — 答辩问答预演（新增★）

**格式**：Q → 推荐回答

**必须准备的问题（15+ 个）**：

**架构类**：
- Q1：为什么分 5 个模块？
- Q2：如果某个模块挂了，其他模块受影响吗？
- Q3：为什么使用 WebSocket 而不是 HTTP？

**规则类**：
- Q4：如何保证规则校验正确？
- Q5：规则校验在客户端还是服务器？
- Q6：暗子和明子在走法上有什么不同？

**AI 类**：
- Q7：AI 是否透视暗子？
- Q8：三档 AI 的本质区别是什么？
- Q9：Alpha-Beta 怎么剪枝？
- Q10：替换表怎么避免哈希冲突？

**工程类**：
- Q11：为什么选 Maven 多模块？
- Q12：怎么保证 AI 不超时？
- Q13：怎么做到一键构建 + 一键自检？

**产品类**：
- Q14：跟别人的棋类项目有什么不同？
- Q15：后续可以怎么扩展？

---

#### 23. FINAL_REPORT.md — 最终大报告（新增★）

**定位**：如果老师只要一份 Word/PDF，这就是完整主体。

**目录结构**：

```
1. 项目简介
   1.1 项目背景
   1.2 项目目标
   1.3 核心功能
   1.4 技术栈
2. 需求分析
   2.1 用户角色
   2.2 功能需求
   2.3 非功能需求
3. 总体架构
   3.1 Maven 多模块结构
   3.2 模块职责与依赖
   3.3 对局主流程
   3.4 核心类图
4. 规则引擎设计
   4.1 棋类模型
   4.2 暗子与明子
   4.3 走法规则
   4.4 终局判定
   4.5 校验流程
5. 网络通信设计
   5.1 WebSocket 协议
   5.2 房间与匹配
   5.3 计时机制
   5.4 TCP 附录 B
6. AI 算法设计
   6.1 三档 AI 对比
   6.2 搜索算法
   6.3 评估函数
   6.4 揭棋信息差处理
7. 客户端设计
   7.1 控制台交互
   7.2 棋盘显示
   7.3 命令系统
8. 棋谱与复盘
   8.1 文字棋谱
   8.2 复盘时间线
   8.3 持久化存储
9. 接口协议
   9.1 公共消息类型
   9.2 本组扩展消息
10. 测试方案与结果
    10.1 测试范围
    10.2 关键用例
    10.3 测试报告
11. 部署与运行
    11.1 环境要求
    11.2 构建
    11.3 运行
    11.4 Docker
12. 项目管理
    12.1 团队分工
    12.2 贡献度
    12.3 AI 辅助说明
13. 总结与展望
    13.1 已完成功能
    13.2 已知限制
    13.3 后续规划
```

**写法要求**：
- 每章 2-4 页
- 多用表格和图表，少用段落文字
- 架构图、类图、流程图必须有
- 不要复制粘贴代码
- 不要出现"我们认为""我们觉得"
- 关键设计决策要写"为什么"

---

## 三、文档间引用关系

```
PROJECT_OVERVIEW.md
  ├── → ARCHITECTURE.md → DOMAIN_MODEL.md
  ├── → RULE_ENGINE_DESIGN.md
  ├── → AI_DESIGN.md
  ├── → REPLAY_DESIGN.md
  ├── → BUILD_AND_RUN.md
  ├── → FEATURE_MATRIX.md
  └── → REQUIREMENTS.md

FINAL_REPORT.md（整合以上所有技术内容）
  ├── → 从各设计文档摘取核心
  ├── → 从 TEST_REPORT.md 摘测试结果
  └── → 从 TEAM.md 摘分工

DEMO_SCRIPT.md（独立，面向演示现场）
DEFENSE_QA.md（独立，面向答辩提问）
```

---

## 四、执行顺序与优先级

### 第一批（先写，最影响验收印象）

| 顺序 | 文件 | 预估时间 | 原因 |
|---|---|---|---|
| 1 | `RULE_ENGINE_DESIGN.md` | 2.5h | 揭棋最复杂的部分，老师必看 |
| 2 | `AI_DESIGN.md` | 2h | 算法加分核心，答辩必讲 |
| 3 | `FEATURE_MATRIX.md` | 1h | 一页讲清全部完成度 |
| 4 | `PROJECT_OVERVIEW.md` | 1h | 所有文档的入口 |
| 5 | `DOMAIN_MODEL.md` | 1.5h | 面向对象课程，老师看类设计 |
| 6 | `BUILD_AND_RUN.md` | 1h | 让别人能跑起来 |
| 7 | `TEST_CASES.md` + `TEST_REPORT.md` | 2h | 证明系统稳定 |
| 8 | `DEMO_SCRIPT.md` | 1h | 防止现场紧张忘流程 |
| 9 | `DEFENSE_QA.md` | 1.5h | 15 个预期问题 + 回答 |
| 10 | `FINAL_REPORT.md` | 3h | 最终大报告，整合所有内容 |

### 第二批（增强厚度）

| 顺序 | 文件 | 预估时间 |
|---|---|---|
| 11 | `REQUIREMENTS.md`（重写） | 1.5h |
| 12 | `ACCEPTANCE_CRITERIA.md` | 1h |
| 13 | `REPLAY_DESIGN.md` | 1.5h |
| 14 | `MESSAGE_EXAMPLES.md` | 1h |
| 15 | `ARCHITECTURE.md`（深化） | 1h |
| 16 | `GLOSSARY.md` | 0.5h |

### 第三批（锦上添花）

| 顺序 | 文件 | 预估时间 |
|---|---|---|
| 17 | `PRODUCT_REQUIREMENTS.md` | 1h |
| 18 | `USER_JOURNEY.md` | 0.5h |
| 19 | `COMPETITOR_ANALYSIS.md` | 0.5h |
| 20 | `DOCKER_DEPLOYMENT.md` | 0.5h |
| 21 | `TEST_PLAN.md` | 0.5h |
| 22 | `TROUBLESHOOTING.md` | 0.5h |

### 清理

| 操作 | 文件 | 原因 |
|---|---|---|
| 删除 | `CODEBASE_AND_TEACHER_MIGRATION_PLAN.md` | 过渡产物 |
| 删除 | `CURSOR_TASKS.md` | 开发过程产物 |
| 合并 | `MANUAL_TESTING_MAIN_TCP.md` + `MANUAL_TESTING_TEACHER_WS.md` | → `TEST_REPORT.md` |
| 删除 | `TEACHER_WS_PROTOCOL.md` | 与 INTERFACE.typ 重复 |
| 删除 | `_v2_extract.typ`, `_INTERFACE_v2_latest.typ`, `_teacher_interface.md`, `_build_interface_v31.py` | 旧版本/构建脚本，不入库 |
| 删除 | `TASKS.md` | 已被 fupantasks/suanfatasks/chanpintasks 取代 |
| 保留 | `COMPLETION_REPORT.md` | 但移到 `05-testing/` 下 |
| 保留 | `mvn-test-output.txt` | 移到 `05-testing/` 下 |
| 保留 | `INTEROP.md` | 移到 `03-interface/` 下 |

---

## 五、文档质量检查清单

每写完一份文档，对照这个清单自查：

- [ ] 目标读者是谁？（老师 / 同学 / 用户？）
- [ ] 有没有超过 10 行的纯文字段落？（有则拆成列表/表格）
- [ ] 有没有"已实现""完美""完全"等绝对化用词？（有则改为准确描述）
- [ ] 有没有 Mermaid 图或 ASCII 图？（设计文档必须有图）
- [ ] 有没有代码片段？（只允许 BUILD_AND_RUN 有命令行，其他不要贴 Java 代码）
- [ ] 有没有未经统计的数字？（类数、行数必须用 cloc 实测）
- [ ] 和 INTERFACE.typ 有无矛盾？（有则以 INTERFACE.typ 为准）
- [ ] 和其他文档的引用链接是否有效？

---

## 六、最终交付清单

| # | 文件 | 类别 | 优先级 |
|---|---|---|---|
| 1 | `00-overview/PROJECT_OVERVIEW.md` | 验收报告 | ★★★ |
| 2 | `00-overview/FEATURE_MATRIX.md` | 验收报告 | ★★★ |
| 3 | `00-overview/GLOSSARY.md` | 验收报告 | ★☆☆ |
| 4 | `01-requirements/REQUIREMENTS.md` | 需求分析 | ★★☆ |
| 5 | `01-requirements/ACCEPTANCE_CRITERIA.md` | 需求分析 | ★★☆ |
| 6 | `02-design/ARCHITECTURE.md` | 技术设计 | ★★☆ |
| 7 | `02-design/DOMAIN_MODEL.md` | 技术设计 | ★★★ |
| 8 | `02-design/RULE_ENGINE_DESIGN.md` | 技术设计 | ★★★ |
| 9 | `02-design/AI_DESIGN.md` | 技术设计 | ★★★ |
| 10 | `02-design/REPLAY_DESIGN.md` | 技术设计 | ★★☆ |
| 11 | `03-interface/INTERFACE.typ` | 接口协议 | （已有） |
| 12 | `03-interface/MESSAGE_EXAMPLES.md` | 接口协议 | ★★☆ |
| 13 | `04-deployment/BUILD_AND_RUN.md` | 工程交付 | ★★★ |
| 14 | `04-deployment/DOCKER_DEPLOYMENT.md` | 工程交付 | ★☆☆ |
| 15 | `04-deployment/TROUBLESHOOTING.md` | 工程交付 | ★☆☆ |
| 16 | `05-testing/TEST_PLAN.md` | 测试证明 | ★☆☆ |
| 17 | `05-testing/TEST_CASES.md` | 测试证明 | ★★★ |
| 18 | `05-testing/TEST_REPORT.md` | 测试证明 | ★★★ |
| 19 | `06-product/PRODUCT_REQUIREMENTS.md` | 产品体验 | ★☆☆ |
| 20 | `06-product/USER_JOURNEY.md` | 产品体验 | ★☆☆ |
| 21 | `06-product/COMPETITOR_ANALYSIS.md` | 产品体验 | ★☆☆ |
| 22 | `07-presentation/DEMO_SCRIPT.md` | 答辩材料 | ★★★ |
| 23 | `07-presentation/DEFENSE_QA.md` | 答辩材料 | ★★★ |
| 24 | `07-presentation/FINAL_REPORT.md` | 答辩材料 | ★★★ |

**必交 = 10 份**（★★★），建议交 = 8 份（★★☆），选交 = 6 份（★☆☆）。

---

*文档版本：v1.0 · 2026-06-18 · 张恒基 (Bosprimigenious)*

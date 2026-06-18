# 答辩演示脚本（6–8 分钟）

> **项目**：Unveil 揭棋对弈系统  
> **读者**：答辩主讲人、计时员  
> **关联文档**：[BUILD_AND_RUN.md](../04-deployment/BUILD_AND_RUN.md) · [DEFENSE_QA.md](./DEFENSE_QA.md) · [MESSAGE_EXAMPLES.md](../03-interface/MESSAGE_EXAMPLES.md)

---

## 1. 演示前准备

| 检查项 | 操作 |
|--------|------|
| JDK 21 | `java -version` |
| 依赖已构建 | 至少执行过一次 `mvn package -pl jieqi-app -am -DskipTests` |
| 端口空闲 | 8887 未被占用 |
| 三个终端或 demo 脚本 | 准备好分屏或 `scripts/demo.ps1` |
| 备用方案 | Fat JAR 路径 `jieqi-app/target/unveil-jieqi.jar` 已存在 |

**建议窗口布局**：左屏终端（自检 + 服务器日志），右屏上下分屏（两个客户端）。

---

## 2. 时间线脚本

总时长控制在 **6–8 分钟**，可按现场情况压缩「非法走法」或「AI 对战」环节。

| 时间 | 操作 | 说词（参考） | 预期结果 |
|------|------|--------------|----------|
| **0:00** | 运行 `powershell -File scripts/verify.ps1` | 「首先运行组内自检脚本，自动完成单元测试、全模块编译和 Fat JAR 打包。」 | 终端输出 `OK: verify passed` |
| **0:30** | 可选：展示 `mvn exec:java -f jieqi-app/pom.xml -am` 菜单 | 「项目提供统一启动入口，集成 WebSocket 服务器、客户端、本地人机、AI 性能测试等九种模式。」 | 显示 1–9 菜单后退出或切到后台 |
| **1:00** | 终端 1：启动 WS 服务器 | 「接下来启动 WebSocket 服务器，采用课程公共接口，默认端口 8887，消息格式为 JSON。」 | `[WsGameServer] 监听 ws://0.0.0.0:8887` |
| **1:30** | 终端 2：客户端 player1 | 「第一位玩家连接服务器，自动完成 Login 认证。」 | `loginResult.success=true`，显示命令帮助 |
| **2:00** | 终端 3：客户端 player2 | 「第二位玩家同样登录，双方进入匹配队列。」 | 登录成功 |
| **2:30** | 双方输入 `match` → `ready` → `first` | 「双方匹配成功后准备就绪，并在十秒窗口内协商先手，服务器下发 gameStart 和初始棋盘。」 | 收到 `matchSuccess`、`gameStart`，棋盘显示 |
| **3:00** | 先手走一匹马（如 `move b 0 b 3`） | 「演示合法走子：暗子按所在位置的虚拟类型行棋，移动后由服务器权威翻子并广播 moveResult。」 | 双方棋盘同步，可能有 `flipResult` |
| **3:30** | 故意发送非法走法（如送将） | 「若客户端提交非法着法，服务器拒绝执行，仅向发送方返回 valid=false 和错误码，对手棋盘不受影响。」 | 发送方看到错误提示，对方无变化 |
| **4:00** | 单端输入 `ai medium` 或展示菜单 6 本地人机 | 「系统提供三档 AI：Easy 启发式、Medium Alpha-Beta、Hard 信念采样；AI 仅使用公开视角，不透视对手暗子。」 | AI 在数秒内返回合法着法 |
| **5:00** | 触发终局（将死 / 认输 `resign` / 超时演示口述） | 「终局后服务器推送 gameOver，包含胜负原因、吃子揭晓和棋谱保存路径。」 | `gameOver` + 终局摘要 |
| **5:30** | 输入 `replay`，用 `n`/`p` 切换帧 | 「复盘不依赖重放棋谱文本，而是回放服务器保存的棋盘快照时间线，保证翻子结果与对局一致。」 | `replayFrame` 逐步显示历史局面 |
| **6:00** | 输入 `rematch` 或展示 `records/*.replay.json` | 「支持终局后再来一局；复盘数据同时落盘为 replay.json，房间销毁后仍可查阅。」 | rematch 流程或文件路径展示 |
| **6:30** | 总结（可配合架构图一页 PPT） | 「Unveil 实现了揭棋规则引擎、WebSocket 网络对弈、三档 AI、棋谱与复盘、Maven 多模块工程化，协议对齐 INTERFACE v3.0。」 | — |
| **7:00–8:00** | 预留问答缓冲 | 「以上为核心演示，详细设计见最终报告与 INTERFACE 协议文档。」 | — |

---

## 3. 关键命令备忘

```
# 自检
powershell -File scripts/verify.ps1

# 一键三窗口（Windows）
powershell -File scripts/demo.ps1

# 服务器
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=server-ws 8887"

# 客户端
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=client-ws ws://127.0.0.1:8887 player1 123456"

# 对局内
match
ready
first true    # 或 false
move b 0 b 3
replay
n / p / g 5
rematch
```

---

## 4. 风险与备选话术

| 风险 | 备选方案 | 话术 |
|------|----------|------|
| Maven 首次下载慢 | 提前构建好，直接用 Fat JAR | 「构建已在赛前完成，这里直接启动打包产物。」 |
| 匹配/网络卡住 | 改用本地人机（菜单 6） | 「网络环节展示协议设计，本地模式演示规则与 AI。」 |
| AI 思考过久 | 改用 `ai easy` | 「Easy 档毫秒级响应，Hard 档展示信念采样算法。」 |
| verify 失败 | 展示最近一次通过的 `mvn-test-output.txt` | 「今早全量测试已通过，现场环境差异导致单测失败，报告中有完整日志。」 |

---

## 5. 演示亮点对照（供评委）

| 课程要求 | 演示环节 |
|----------|----------|
| 网络双人对弈 | 双客户端 WS 对局 |
| 服务器规则校验 | 非法走法拒绝 |
| 翻子随机 | moveResult 中 flipResult |
| AI 博弈 | 三档 AI / ai 命令 |
| 棋谱记录 | 终局摘要中的 `.jieqi` 路径 |
| 复盘 | replay 时间线 |
| 工程化 | verify.ps1 + Maven 多模块 |

---

*文档版本：v1.0 · 2026-06-18 · Unveil 第一组*

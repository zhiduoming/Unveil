# 构建与运行指南

> **项目**：Unveil 揭棋对弈系统  
> **读者**：验收老师、联调同学、组内开发者  
> **关联文档**：[DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) · [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) · [../03-interface/MESSAGE_EXAMPLES.md](../03-interface/MESSAGE_EXAMPLES.md)

---

## 1. 环境要求

| 组件 | 版本要求 | 检查命令 | 预期输出要点 |
|------|----------|----------|--------------|
| JDK | **21**（与 `pom.xml`、CI 一致） | `java -version` | `openjdk version "21"` 或 `21.0.x` |
| Maven | **3.9+** | `mvn -version` | `Apache Maven 3.9.x`，Java version: 21 |
| 操作系统 | Windows / macOS / Linux | — | PowerShell 或 Bash 均可 |
| 网络 | 本机联调需放行 8887（WS）、8888（TCP 可选） | — | 防火墙允许入站 |

### 1.1 JDK 21 配置要点

- `JAVA_HOME` 必须指向 JDK 21 安装目录，不能指向 JRE 或旧版 JDK。
- Windows PowerShell 快速检查：`echo $env:JAVA_HOME`；CMD：`echo %JAVA_HOME%`。
- 若 `mvn -version` 显示的 Java 版本不是 21，请修正 `JAVA_HOME` 后重开终端。

### 1.2 可选工具

| 工具 | 用途 |
|------|------|
| Docker + Docker Compose | 容器化部署 WebSocket 服务器 |
| Typst | 编译 `docs/INTERFACE.typ` 为 PDF |
| Git | 克隆仓库与版本管理 |

---

## 2. 获取源码

```bash
git clone <仓库地址> Unveil
cd Unveil
```

仓库根目录应包含 `pom.xml` 及五个 Maven 子模块：`jieqi-core`、`jieqi-server`、`jieqi-client`、`jieqi-ai`、`jieqi-app`。

---

## 3. 构建

### 3.1 推荐：全量打包（跳过测试，最快产出可运行 JAR）

```bash
mvn clean package -DskipTests
```

### 3.2 构建步骤说明表

| 步骤 | 命令 | 作用 | 产物 |
|------|------|------|------|
| 清理 | `mvn clean` | 删除各模块 `target/` | — |
| 编译 | `mvn compile` | 编译全部模块源码 | `target/classes/` |
| 单元测试 | `mvn test` | 运行 JUnit 5 用例 | `surefire-reports/` |
| 打包 | `mvn package -pl jieqi-app -am` | 构建 Fat JAR 及依赖 | `jieqi-app/target/unveil-jieqi.jar` |
| 一键自检 | `powershell -File scripts/verify.ps1` | 测试 + 编译 + 打包 | 终端输出 `OK: verify passed` |

### 3.3 预期成功输出

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for unveil-jieqi 1.0.0-SNAPSHOT:
[INFO]
[INFO] jieqi-core ......................................... SUCCESS
[INFO] jieqi-server ....................................... SUCCESS
[INFO] jieqi-client ....................................... SUCCESS
[INFO] jieqi-ai ........................................... SUCCESS
[INFO] jieqi-app .......................................... SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Fat JAR 路径：`jieqi-app/target/unveil-jieqi.jar`（约数十 MB，含全部依赖）。

---

## 4. 运行模式总览

统一入口类：`com.jieqi.app.Main`（`jieqi-app` 模块）。  
推荐 Maven 调用方式（自动构建依赖模块）：

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="<子命令> [参数...]"
```

或使用 Fat JAR（避免本地 `~/.m2` 旧 SNAPSHOT 干扰）：

```bash
java -jar jieqi-app/target/unveil-jieqi.jar <子命令> [参数...]
```

### 4.1 全部运行模式对照表

| 模式 | CLI 子命令 / 菜单项 | 完整命令示例 | 预期行为 |
|------|---------------------|--------------|----------|
| **交互菜单** | 无参数启动 | `mvn exec:java -f jieqi-app/pom.xml -am` | 显示 1–9 功能菜单 |
| **WebSocket 服务器** | `server-ws [端口]` / 菜单 **3** | `mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=server-ws 8887"` | 监听 **8887**，等待 JSON 连接 |
| **WebSocket 客户端** | `client-ws <url> <user> <pass>` / 菜单 **4** | `mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=client-ws ws://127.0.0.1:8887 player1 123456"` | 连接服务器，进入交互命令行 |
| **AI 经 WebSocket 自动对弈** | `ai-ws <url> <user> <pass>` / 菜单 **9** | `mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=ai-ws ws://127.0.0.1:8887 ai_bot_1 pw123"` | AI 客户端自动登录、匹配、走子 |
| **TCP 服务器（附录 B）** | `server [端口]` / 菜单 **1** | `mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=server 8888"` | 监听 **8888**，文本帧协议 |
| **TCP 客户端** | `client <host> <port> <name>` / 菜单 **2** | `mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=client 127.0.0.1 8888 player1"` | 连接 TCP 服务器 |
| **本地 AI vs AI** | 菜单 **5** | 在交互菜单选择 5 | 本地双 AI 对弈，无网络 |
| **本地人 vs AI** | 菜单 **6** | 在交互菜单选择 6，再选难度 1–3 | 控制台人机，三档难度 |
| **AI 性能测试** | 菜单 **7** | 在交互菜单选择 7 | 输出各档 AI 步时统计 |
| **本地规则测试** | 菜单 **8** | 在交互菜单选择 8 | 单盘本地走子调试 |
| **Fat JAR 直接运行** | 同上子命令 | `java -jar jieqi-app/target/unveil-jieqi.jar server-ws 8887` | 与 exec:java 等价，启动更快 |

### 4.2 交互菜单一览（无参数启动）

```
=================================
   揭棋对弈系统 (Unveil)
=================================
--- WebSocket + JSON（推荐，老师协议 8887）---
3. 启动 WebSocket 服务器
4. 启动 WebSocket 客户端
9. AI 经 WebSocket 自动对弈
--- TCP 附录 B（可选调试 8888）---
1. 启动 TCP 服务器
2. 启动 TCP 客户端
--- 本地 / AI ---
5. AI vs AI 自动对弈
6. 人 vs AI 对弈（本地）
7. AI 性能测试
8. 本地测试模式
请选择 (1-9):
```

---

## 5. WebSocket 双人对弈完整演示流程

以下流程适用于验收现场或组内联调，共需 **三个终端**。

### 5.1 方式 A：一键演示脚本（Windows 推荐）

```powershell
powershell -File scripts/demo.ps1
```

脚本行为：

1. 新开 PowerShell 窗口 A：启动 WebSocket 服务器（端口 8887）
2. 等待 3 秒
3. 新开窗口 B：客户端 `player1` / `123456`
4. 新开窗口 C：客户端 `player2` / `123456`

终端提示：`三个窗口已启动，可以开始演示。`

### 5.2 方式 B：手动分步操作

#### 终端 1 — 启动服务器

```bash
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=server-ws 8887"
```

**预期输出**（示意）：

```
[WsGameServer] 监听 ws://0.0.0.0:8887
```

#### 终端 2 — 玩家 1

```bash
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=client-ws ws://127.0.0.1:8887 player1 123456"
```

**预期**：自动发送 `Login`，收到 `loginResult.success=true`，显示命令帮助。

#### 终端 3 — 玩家 2

```bash
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=client-ws ws://127.0.0.1:8887 player2 123456"
```

#### 对局流程命令（双方交替输入）

| 步骤 | 操作方 | 客户端命令 | 预期系统响应 |
|------|--------|------------|--------------|
| 1 | 双方 | `match` | 收到 `matchSuccess`，分配 `roomId` |
| 2 | 双方 | `ready` | 收到 `roomInfo.opponentReady=true` |
| 3 | 双方 | `first true` / `first false` | 10 秒窗口内协商先手 |
| 4 | 系统 | — | 双方收到 `gameStart`，显示 `initialBoard` |
| 5 | 先手 | `move b 0 b 3`（示例：马走日） | 双方收到 `moveResult.valid=true` |
| 6 | 后手 | 合法走子 | 棋盘同步更新 |
| 7 | 任一方 | 非法走子（如送将） | 仅发送方收到 `moveResult.valid=false` + `error` |
| 8 | 终局 | — | 收到 `gameOver`，显示胜者、原因、棋谱路径 |
| 9 | 双方 | `replay` | 进入复盘，`n`/`p`/`g 12` 切换帧 |
| 10 | 双方 | `rematch` | 发送 `rematchRequest`，协商新局 |

坐标约定：**列 a–i（左→右），行 0–9（0=红方底线，9=黑方底线）**。  
走子格式：`move <fromX> <fromY> <toX> <toY>`，例如 `move a 6 a 5`。

### 5.3 人机对弈（单客户端）

在已连接服务器的客户端中输入：

```
ai easy      # 简单：启发式 + 随机
ai medium    # 中等：Alpha-Beta 搜索
ai hard      # 困难：Belief Sampling + Alpha-Beta
```

系统匹配 AI 对手后流程与真人对弈相同。

---

## 6. 自检脚本 verify.ps1

**路径**：`scripts/verify.ps1`  
**用途**：监工验收、联调前快速确认「测试通过 + 全模块编译 + Fat JAR 可打包」。

### 6.1 运行方式

```powershell
powershell -File scripts/verify.ps1
```

须在仓库根目录执行（脚本会自动 `cd` 到父目录）。

### 6.2 执行步骤

| 顺序 | 动作 | 命令 |
|------|------|------|
| 1 | 核心模块单元测试 | `mvn -q test -pl jieqi-core,jieqi-server,jieqi-ai` |
| 2 | 全模块编译 | `mvn -q compile` |
| 3 | 打包 Fat JAR | `mvn -q package -pl jieqi-app -am -DskipTests` |

### 6.3 成功标志

```
==> mvn test (jieqi-core, jieqi-server, jieqi-ai)
==> mvn compile (all modules)
==> mvn package (jieqi-app, skip tests)
OK: verify passed
```

任一步失败则脚本以非零退出码终止，需运行 `mvn test` 查看具体失败用例（参见 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)）。

---

## 7. 演示脚本 demo.ps1

**路径**：`scripts/demo.ps1`  
**用途**：验收现场一键拉起「服务器 + 双客户端」三个独立窗口，避免手动复制命令。

### 7.1 运行前提

- 已完成 `mvn package` 或至少 `mvn compile`（首次运行 Maven 会下载依赖，耗时较长）
- 端口 8887 未被占用
- Windows PowerShell 允许 `Start-Process` 弹窗

### 7.2 与 verify.ps1 的配合建议

验收演示推荐顺序：

1. `powershell -File scripts/verify.ps1` — 证明测试与构建通过
2. `powershell -File scripts/demo.ps1` — 拉起三窗口进行真人对弈演示

---

## 8. 其他常用命令

| 场景 | 命令 |
|------|------|
| 仅编译不测试 | `mvn compile -f pom.xml` |
| 运行全部测试 | `mvn test` |
| 编译协议 PDF | `typst compile docs/INTERFACE.typ docs/INTERFACE.pdf` |
| Windows 菜单启动 | `powershell -File scripts/run-app.ps1` |
| 开发用 WS 服务器 | `powershell -File scripts/dev-server.ps1 8887` |

---

## 9. 文档导航

| 文档 | 内容 |
|------|------|
| [DOCKER_DEPLOYMENT.md](./DOCKER_DEPLOYMENT.md) | Docker Compose 部署 |
| [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) | 常见问题排查 |
| [../03-interface/INTERFACE.typ](../INTERFACE.typ) | 权威协议 v3.0 |
| [../07-presentation/DEMO_SCRIPT.md](../07-presentation/DEMO_SCRIPT.md) | 6–8 分钟答辩演示脚本 |
| [../../README.md](../../README.md) | 仓库快速入门 |

---

*文档版本：v1.0 · 2026-06-18 · Unveil 第一组*

# Unveil — 揭棋对弈程序

大作业：**揭棋对弈程序设计**（网络真人对弈 + AI 博弈）。

## 仓库结构

```
Unveil/
├── docs/                    # 需求、架构、协议 v3.0、分工
├── pom.xml                  # Maven 父工程
├── jieqi-core/              # 领域、规则、JSON/TCP 协议、UI、棋谱
├── jieqi-server/            # WebSocket 服务器 + TCP 服务器
├── jieqi-client/            # WebSocket 客户端 + TCP 客户端
├── jieqi-ai/                # Alpha-Beta、评估、Agent
└── jieqi-app/               # 统一启动入口
```

详细说明见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 环境

- JDK **21**
- Maven 3.9+

## 自检（监工/联调前）

```powershell
powershell -File scripts/verify.ps1
```

## 构建与运行

```bash
# 方式 A：脚本（推荐，Windows / 自动构建依赖）
powershell -File scripts/run-app.ps1

# 方式 B：Maven（-f 指向 jieqi-app，-am 自动构建依赖模块）
mvn exec:java -f jieqi-app/pom.xml -am

# 方式 C：Fat JAR
mvn package -pl jieqi-app -am -DskipTests
java -jar jieqi-app/target/unveil-jieqi.jar server-ws 8887
```

### WebSocket + JSON（课程公共接口，默认）

```bash
# 服务器（端口 8887）
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server-ws 8887"

# 人机客户端
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client-ws ws://127.0.0.1:8887 player1 123456"

# AI 自动对弈（WS）
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="ai-ws ws://127.0.0.1:8887 ai_bot_1 pw123"
```

或菜单选项 **3 / 4 / 9**。

### Docker（WS 8887）

```bash
docker compose up --build
```

### TCP 文本帧 v2.0（附录 B，兼容调试，端口 8888）

```bash
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="server 8888"
mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args="client 127.0.0.1 8888 Player1"
```

### 编译协议 PDF

```bash
typst compile docs/INTERFACE.typ docs/INTERFACE.pdf
```

## 文档

| 文件 | 内容 |
|------|------|
| [INTERFACE.typ / .md / .pdf](docs/INTERFACE.typ) | **v3.0** WebSocket JSON 权威协议 |
| [REQUIREMENTS.md](docs/REQUIREMENTS.md) | 需求覆盖度 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 架构 |
| [INTEROP.md](docs/INTEROP.md) | 组间联调 |
| [REPORT.md](docs/REPORT.md) | 实验报告 |

## 协议说明

- **正文（组间互操作）**：WebSocket + JSON，`messageType` 字段，端口 **8887**
- **附录 B**：TCP `msgType|len|payload\n`，端口 **8888**
- 联调前与对方约定使用哪一种，不可混用

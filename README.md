# Unveil — 揭棋对弈程序

大作业：**揭棋对弈程序设计**（网络真人对弈 + AI 博弈）。

## 仓库结构

```
Unveil/
├── docs/                    # 需求、架构、协议、分工
├── pom.xml                  # Maven 父工程
├── jieqi-core/              # 领域、规则、协议、UI、棋谱
├── jieqi-server/            # TCP 服务器
├── jieqi-client/            # TCP 客户端
├── jieqi-ai/                # Alpha-Beta、评估、Agent
└── jieqi-app/               # 统一启动入口
```

详细说明见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 环境

- JDK **21**
- Maven 3.9+

## 构建与运行

```bash
# 编译全部模块
mvn compile

# 打包可执行 fat jar（含依赖）
mvn package -pl jieqi-app -am

# 交互菜单（服务器 / 客户端 / AI 等）
mvn exec:java -pl jieqi-app

# 或直接运行 fat jar
java -jar jieqi-app/target/unveil-jieqi.jar
```

### 命令行快捷方式

```bash
mvn exec:java -pl jieqi-app -Dexec.args="server 8888"
mvn exec:java -pl jieqi-app -Dexec.args="client 127.0.0.1 8888 Player1"
```

### 分模块启动

```bash
mvn exec:java -pl jieqi-server -Dexec.mainClass=com.jieqi.server.GameServer
mvn exec:java -pl jieqi-client -Dexec.mainClass=com.jieqi.client.GameClient
```

默认端口：`8888`。

## 模块依赖

```
jieqi-app → server, client, ai, core
jieqi-server / jieqi-client / jieqi-ai → jieqi-core
```

## 文档

| 文件 | 内容 |
|------|------|
| [REQUIREMENTS.md](docs/REQUIREMENTS.md) | 功能与非功能需求 |
| [INTERFACE.md](docs/INTERFACE.md) | TCP / Move 组间协议 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 架构与领域类 |
| [TEAM.md](docs/TEAM.md) | 小组分工模板 |

## 包名约定（组间互操作）

- 逻辑模型与协议：`com.jieqi.core.*`、`com.jieqi.protocol.Protocol`
- 线格式以 `docs/INTERFACE.md` 为准，与具体包路径无关

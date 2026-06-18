#import "../template.typ": *
#show: doc => [ #cover(title: "Docker 部署指南", subtitle: "Deployment — Docker Compose 一键部署", doc-type: "工程交付") #doc ]
#setup-doc(title: "Unveil — Docker 部署指南")

= 概述

本项目提供基于 *Docker Compose* 的一键部署方案，默认启动 *WebSocket JSON 服务器*（课程公共接口，端口 *8887*）。

= 前置条件

#table(
  columns: (auto, auto, auto),
  [*组件*], [*版本建议*], [*检查命令*],
  [Docker Engine], [24+], [`docker --version`],
  [Docker Compose], [v2], [`docker compose version`],
  [磁盘空间], [≥ 2 GB], [—],
  [端口], [8887 未被占用], [`netstat -ano | findstr 8887`],
)

= 仓库文件说明

#table(
  columns: (auto, auto),
  [*文件*], [*作用*],
  [`Dockerfile`], [多阶段构建：Maven 21 编译 → JRE 21 运行 Fat JAR],
  [`docker-compose.yml`], [定义 jieqi-server 服务与端口映射],
  [`jieqi-app/target/unveil-jieqi.jar`], [运行时入口],
)

== Dockerfile 构建阶段

#table(
  columns: (auto, auto, auto),
  [*阶段*], [*基础镜像*], [*动作*],
  [build], [maven:3.9.9-eclipse-temurin-21], [mvn package -pl jieqi-app -am -DskipTests],
  [run], [eclipse-temurin:21-jre], [java -jar unveil-jieqi.jar server-ws 8887],
)

= 构建与启动

== 一键构建并启动（推荐）

```bash
docker compose up --build
```

#table(
  columns: (auto, auto),
  [*参数*], [*含义*],
  [`--build`], [构建或重建镜像后再启动],
  [`-d`], [后台运行（可选）],
  [`--force-recreate`], [强制重建容器],
)

预期日志：`[WsGameServer] 监听 ws://0.0.0.0:8887`

== 分步操作

```bash
docker compose build     # 仅构建镜像
docker compose up -d     # 后台启动
docker compose ps        # 查看运行状态
```

= 端口映射说明

#table(
  columns: (auto, auto, auto, auto, auto),
  [*协议*], [*容器内端口*], [*宿主机默认*], [*用途*], [*Compose 是否映射*],
  [WebSocket JSON], [8887], [8887], [课程公共接口、组间互操作], [是],
  [TCP 文本帧 v2.0], [8888], [8888], [附录 B 调试、兼容旧客户端], [否],
)

= TCP 8888 端口（附录 B）

当前 Dockerfile 仅启动 `server-ws 8887`，不含 TCP 服务器。建议 Docker 仅部署 WS；TCP 8888 在开发机本地运行：

```bash
mvn exec:java -f jieqi-app/pom.xml -am "-Dexec.args=server 8888"
```

= 日志查看

```bash
docker compose logs -f jieqi-server          # 跟踪日志
docker compose logs --tail=100 jieqi-server  # 最近 100 行
```

= 停止与清理

```bash
docker compose down               # 停止容器
docker compose down -v            # 停止并删除卷
docker compose down --rmi local   # 删除构建的镜像
```

= 与本地构建对比

#table(
  columns: (auto, auto, auto),
  [*维度*], [*Docker Compose*], [*本地 mvn exec:java*],
  [环境依赖], [仅需 Docker], [JDK 21 + Maven 3.9+],
  [构建时间], [首次较慢（缓存后加快）], [依赖本机 ~/.m2],
  [适用场景], [验收机无 Java、快速演示], [开发调试、运行客户端与 AI],
  [协议支持], [默认仅 WS 8887], [WS 8887 + TCP 8888],
)

= 故障排查

#table(
  columns: (auto, auto, auto),
  [*现象*], [*可能原因*], [*处理*],
  [port is already allocated], [8887 被占用], [改 JIEQI_PORT 或释放端口],
  [BUILD FAILURE], [网络无法拉取 Maven 依赖], [配置 Docker 代理或重试],
  [客户端连不上], [用了 localhost 但客户端在容器内], [用服务名或宿主机 IP],
  [容器启动后立即退出], [JAR 路径错误或端口绑定失败], [docker compose logs 查看堆栈],
)

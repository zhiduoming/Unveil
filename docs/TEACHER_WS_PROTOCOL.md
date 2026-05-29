# WebSocket 协议说明（已合并至 INTERFACE v3.0）

本文件内容已并入权威协议文档，请优先阅读：

- **[INTERFACE.typ](./INTERFACE.typ)** / **[INTERFACE.md](./INTERFACE.md)** — v3.0 正文（WebSocket + JSON）
- **附录 B** — 本组可选 TCP v2.0 扩展

## 快速启动

```bash
mvn exec:java -pl jieqi-server -Dexec.mainClass="com.jieqi.server.ws.WsGameServer" -Dexec.args="8887"
mvn exec:java -pl jieqi-client -Dexec.mainClass="com.jieqi.client.WsGameClient" -Dexec.args="ws://127.0.0.1:8887 alice 123"
```

客户端命令：`match` → `ready` → `move b 1 b 3` → `resign` / `ping` / `quit`

实现包：`com.jieqi.protocol.json`、`com.jieqi.server.ws`、`com.jieqi.client.WsGameClient`

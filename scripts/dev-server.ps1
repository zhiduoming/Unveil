# 本地开发：打包 Fat JAR 并启动 WS 服务端（8887）
# 勿用 mvn exec:java —— 会加载 ~/.m2 里过期的 SNAPSHOT，导致 startAiGame 等扩展消息不可用。
# 用法: powershell -File scripts/dev-server.ps1 [port]
$port = if ($args.Count -gt 0) { $args[0] } else { "8887" }
& (Join-Path $PSScriptRoot "run-app.ps1") "server-ws" $port

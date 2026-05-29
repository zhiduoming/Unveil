# 启动 jieqi-app（先构建依赖模块，再运行交互菜单）
# 用法:
#   powershell -File scripts/run-app.ps1
#   powershell -File scripts/run-app.ps1 server-ws 8887
#   powershell -File scripts/run-app.ps1 client-ws ws://127.0.0.1:8887 alice 123
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "==> mvn package -pl jieqi-app -am -DskipTests"
mvn -q package -pl jieqi-app -am -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$jar = Join-Path (Get-Location) "jieqi-app\target\unveil-jieqi.jar"
if (-not (Test-Path $jar)) {
    Write-Error "未找到 $jar，请先执行 mvn package -pl jieqi-app -am"
}

if ($args.Count -gt 0) {
    & java -jar $jar @args
} else {
    & java -jar $jar
}
exit $LASTEXITCODE

# 组内自检：编译 + 单元测试（监工/联调前执行）
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "==> mvn test (jieqi-core, jieqi-server, jieqi-ai)"
mvn -q test -pl jieqi-core,jieqi-server,jieqi-ai
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "==> mvn compile (all modules)"
mvn -q compile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "OK: verify passed"

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

function Resolve-Java21Plus {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return (Join-Path $env:JAVA_HOME "bin\java.exe")
    }
    $candidates = @(
        "C:\Program Files\Java\jdk-24\bin\java.exe",
        "C:\Program Files\Java\jdk-21\bin\java.exe",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-25.0.3.9-hotspot\bin\java.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    return "java"
}

$javaExe = Resolve-Java21Plus
Write-Host "==> java: $javaExe"

if ($args.Count -gt 0) {
    & $javaExe -jar $jar @args
} else {
    & $javaExe -jar $jar
}
exit $LASTEXITCODE

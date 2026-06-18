# 演示脚本：启动 WebSocket 服务器 + 两个客户端
# 需要三个独立的终端窗口

Write-Host "启动 WebSocket 服务器 (端口 8887)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='server-ws 8887'"

Start-Sleep -Seconds 3

Write-Host "启动客户端 1 (player1)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='client-ws ws://127.0.0.1:8887 player1 123456'"

Write-Host "启动客户端 2 (player2)..."
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\..'; mvn exec:java -f jieqi-app/pom.xml -am -Dexec.args='client-ws ws://127.0.0.1:8887 player2 123456'"

Write-Host "三个窗口已启动，可以开始演示。"

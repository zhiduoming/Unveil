# Unveil LOC counter — counts Java source files and lines
param([switch]$IncludeTests)

$root = $PSScriptRoot
if (-not $root) {
    $root = Split-Path -Parent (Get-Location).Path
} else {
    $root = Split-Path -Parent $root
}

if ($IncludeTests) {
    $pathPattern = "*src*java*"
    Write-Host "=== Main + Test ==="
} else {
    $pathPattern = "*src\main\java*"
    Write-Host "=== Main (excluding tests) ==="
}

$allFiles = Get-ChildItem -Path $root -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$files = @($allFiles | Where-Object { $_.FullName -like $pathPattern })

$totalLines = 0
foreach ($f in $files) {
    $totalLines += (Get-Content $f.FullName | Measure-Object -Line).Lines
}
$fileCount = $files.Count

Write-Host "Java files : $fileCount"
Write-Host "Total lines: $totalLines"
Write-Host ""

$modules = @("jieqi-core", "jieqi-server", "jieqi-client", "jieqi-ai", "jieqi-app")
foreach ($mod in $modules) {
    $modFiles = @($files | Where-Object { $_.FullName -like "*$mod*" })
    $modLines = 0
    foreach ($mf in $modFiles) {
        $modLines += (Get-Content $mf.FullName | Measure-Object -Line).Lines
    }
    Write-Host "  $mod : $($modFiles.Count) files, $modLines lines"
}

# 批量编译 docs/**/*.typ → PDF（排除模板与旧稿）
# 用法: powershell -File scripts/compile-docs.ps1
param([switch]$Quiet)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$docs = Join-Path $root "docs"

$excludeNames = @("template.typ", "_v2_extract.typ", "_INTERFACE_v2_latest.typ")
$ok = 0
$fail = 0
$skipped = 0

function Compile-Typst {
    param([string]$TypPath, [string]$PdfPath)
    if ($Quiet) {
        typst compile --root $docs $TypPath $PdfPath 2>$null
    } else {
        Write-Host "  typst compile --root docs $TypPath"
        typst compile --root $docs $TypPath $PdfPath
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Host "    ERROR: $TypPath" -ForegroundColor Red
        return $false
    }
    return $true
}

Write-Host "==> Compiling INTERFACE.typ (protocol authority)"
$ifaceTyp = Join-Path $docs "INTERFACE.typ"
$ifacePdf = Join-Path $docs "INTERFACE.pdf"
if (Test-Path $ifaceTyp) {
    if (Compile-Typst $ifaceTyp $ifacePdf) { $ok++ } else { $fail++ }
}

Write-Host "==> Compiling documentation .typ files"
$typFiles = Get-ChildItem $docs -Recurse -Filter "*.typ" |
    Where-Object { $excludeNames -notcontains $_.Name -and $_.Name -ne "INTERFACE.typ" } |
    Sort-Object FullName

foreach ($tf in $typFiles) {
    $pdfPath = [IO.Path]::ChangeExtension($tf.FullName, ".pdf")
    if (Compile-Typst $tf.FullName $pdfPath) { $ok++ } else { $fail++ }
}

Write-Host ""
Write-Host "=== Done: $ok OK, $fail failed, $($typFiles.Count + 1) total attempted ==="
if ($fail -gt 0) { exit 1 }

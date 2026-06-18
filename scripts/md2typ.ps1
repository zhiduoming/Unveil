# Markdown → Typst 批量转换（基于 INTERFACE.typ 模板）
# 用法: powershell -File scripts/md2typ.ps1 [--all] [--compile]
param([switch]$All, [switch]$Compile)

$root = Split-Path -Parent $PSScriptRoot
$templatePath = "$root/docs/template.typ"

function Convert-MdToTypst {
    param($mdPath, $typPath, $title, $subtitle, $docType)
    $lines = Get-Content $mdPath -Encoding UTF8
    $typ = @()
    $typ += "// Auto-generated from $((Get-Item $mdPath).Name)"
    $typ += '#import "../template.typ": *'
    $typ += ""
    $typ += "#show: doc => [ #cover(title: `"$title`", subtitle: `"$subtitle`", doc-type: `"$docType`") #doc ]"
    $typ += ""
    $typ += "#setup-doc(title: `"Unveil — $title`")"
    $typ += ""

    $inCode = $false
    $inTable = $false
    $tableRows = @()
    $tableSepCount = 0

    foreach ($line in $lines) {
        # Code blocks
        if ($line -match '^```') {
            if ($inCode) { $typ += '```'; $inCode = $false }
            else { $inCode = $true; $typ += '```' }
            continue
        }
        if ($inCode) { $typ += $line; continue }

        # Table separator line
        if ($line -match '^\|[-| ]+\|$') { $tableSepCount++; continue }

        # Table rows
        if ($line -match '^\|.+\|$') {
            if (-not $inTable) { $inTable = $true; $tableRows = @() }
            $cells = ($line -replace '^\|' -replace '\|$').Split('|') | ForEach-Object { $_.Trim() }
            $tableRows += ,$cells
            continue
        }

        # Flush table
        if ($inTable -and $tableRows.Count -gt 0) {
            $colCount = ($tableRows | ForEach-Object { $_.Count } | Measure-Object -Maximum).Maximum
            $typ += "#table("
            $typ += "  columns: ($colCount * (1fr,)),"
            $header = $tableRows[0]
            $typ += "  [*$($header -join '*], [*')*],"
            for ($i = 1; $i -lt $tableRows.Count; $i++) {
                $row = $tableRows[$i]
                # Pad row if needed
                while ($row.Count -lt $colCount) { $row += '' }
                $escapedCells = $row | ForEach-Object { $_ -replace '`', '' }
                $typ += "  [$($escapedCells -join '], [')],"
            }
            $typ += ")"
            $typ += ""
            $inTable = $false
            $tableRows = @()
            $tableSepCount = 0
        }

        # Headings
        if ($line -match '^#### (.+)') { $typ += "==== $($Matches[1])"; continue }
        if ($line -match '^### (.+)')  { $typ += "=== $($Matches[1])"; continue }
        if ($line -match '^## (.+)')   { $typ += "== $($Matches[1])"; continue }
        if ($line -match '^# (.+)')    { $typ += "= $($Matches[1])"; continue }

        # Blockquotes
        if ($line -match '^> (.+)') {
            $typ += "#note-box[$($Matches[1])]"
            continue
        }

        # Lists
        if ($line -match '^- (.+)') { $typ += "- $($Matches[1])"; continue }
        if ($line -match '^\d+\. (.+)') { $typ += "+ $($Matches[1])"; continue }

        # Horizontal rules
        if ($line -match '^---') { $typ += "#line(length: 100%)"; continue }

        # Bold / italic
        $line = $line -replace '\*\*(.+?)\*\*', '*$1*'
        $line = $line -replace '`(.+?)`', '`$1`'

        # Links [text](url) → text
        $line = $line -replace '\[(.+?)\]\(.+?\)', '$1'

        # Horizontal rule / separator
        if ($line -match '^---+$') { $line = '#line(length: 100%)' }

        $typ += $line
    }

    # Flush any remaining table
    if ($inTable -and $tableRows.Count -gt 0) {
        $colCount = ($tableRows | ForEach-Object { $_.Count } | Measure-Object -Maximum).Maximum
        $typ += "#table("
        $typ += "  columns: ($colCount * (1fr,)),"
        $header = $tableRows[0]
        $typ += "  [*$($header -join '*], [*')*],"
        for ($i = 1; $i -lt $tableRows.Count; $i++) {
            $row = $tableRows[$i]
            while ($row.Count -lt $colCount) { $row += '' }
            $escapedCells = $row | ForEach-Object { $_ -replace '`', '' }
            $typ += "  [$($escapedCells -join '], [')],"
        }
        $typ += ")"
    }

    $typ -join "`n" | Out-File $typPath -Encoding UTF8
    Write-Host "  OK: $typPath"
}

function Get-DocType {
    param($dir)
    switch ($dir) {
        '00-overview'      { '验收报告' }
        '01-requirements'  { '需求分析' }
        '02-design'        { '技术设计' }
        '03-interface'     { '接口协议' }
        '04-deployment'    { '工程交付' }
        '05-testing'       { '测试证明' }
        '06-product'       { '产品体验' }
        '07-presentation'  { '答辩材料' }
        default            { '技术文档' }
    }
}

# 主转换逻辑
$docDirs = @('00-overview', '01-requirements', '02-design', '03-interface', '04-deployment', '05-testing', '06-product', '07-presentation')
$count = 0

foreach ($dir in $docDirs) {
    $mdFiles = Get-ChildItem "$root/docs/$dir" -Filter "*.md" -ErrorAction SilentlyContinue
    foreach ($md in $mdFiles) {
        $baseName = [IO.Path]::GetFileNameWithoutExtension($md.Name)
        $typPath = "$root/docs/$dir/$baseName.typ"

        # Skip if typ already exists and newer than md (unless --all)
        if (-not $All -and (Test-Path $typPath)) {
            $typTime = (Get-Item $typPath).LastWriteTime
            if ($typTime -ge $md.LastWriteTime) {
                Write-Host "  SKIP: $typPath (up to date)"
                continue
            }
        }

        $docType = Get-DocType $dir
        $title = (Get-Content $md.FullName -Encoding UTF8 -First 5 | Select-String '^# ').ToString() -replace '^# ', ''
        if (-not $title) { $title = $baseName -replace '_', ' ' }

        Convert-MdToTypst -mdPath $md.FullName -typPath $typPath -title $title -subtitle '' -docType $docType
        $count++
    }
}

# Also convert root-level .md files (README, TEAM)
$rootMds = @('README.md', 'TEAM.md')
foreach ($r in $rootMds) {
    $mdPath = "$root/docs/$r"
    if (-not (Test-Path $mdPath)) { continue }
    $typPath = "$root/docs/$([IO.Path]::GetFileNameWithoutExtension($r)).typ"
    $baseName = [IO.Path]::GetFileNameWithoutExtension($r)
    Convert-MdToTypst -mdPath $mdPath -typPath $typPath -title $baseName -subtitle '' -docType '项目文档'
    $count++
}

Write-Host ""
Write-Host "=== Converted $count files ==="

# 编译
if ($Compile) {
    Write-Host "==> Compiling .typ → .pdf..."
    $typFiles = Get-ChildItem "$root/docs" -Recurse -Filter "*.typ" -Exclude "template.typ"
    foreach ($tf in $typFiles) {
        $pdfPath = [IO.Path]::ChangeExtension($tf.FullName, '.pdf')
        Write-Host "  typst compile $($tf.FullName) → $pdfPath"
        typst compile $tf.FullName $pdfPath 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "    ERROR: $($tf.Name)"
        }
    }
    Write-Host "=== Compile done ==="
}

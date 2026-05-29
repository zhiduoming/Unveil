param([string]$Path)

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($Path)
$entry = $zip.GetEntry('word/document.xml')
$stream = $entry.Open()
$reader = New-Object System.IO.StreamReader($stream)
$xml = $reader.ReadToEnd()
$reader.Close()
$stream.Close()
$zip.Dispose()

# Strip XML tags, collapse whitespace
$text = $xml -replace '<[^>]+>', '`n' -replace '`n\s*`n', "`n"
$text = [System.Text.RegularExpressions.Regex]::Replace($text, '\n\s*\n', "`n`n")
Write-Output $text

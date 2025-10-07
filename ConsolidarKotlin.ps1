param(
    [string]$projectPath = (Get-Location).Path,
    [string]$outputPath = [Environment]::GetFolderPath("Desktop"),
    [switch]$includeReadme = $true,
    [switch]$includeTree = $true,
    [switch]$summarizeImports = $true
)

function Get-ProjectTree {
    param([string]$path, [string]$indent = "", [bool]$isLast = $true)
    $item = Get-Item $path
    if ($item.Name -match '^(\.gradle|\.idea|build|\.kotlin|kspCaches|generated|intermediates|outputs|tmp|caches|captures|\.git|temp_processed_files|.*cache.*|.*\.temp|node_modules|dist|out)$') { 
        return "" 
    }
    $result = "$indent" + $(if ($isLast) { "+-- " } else { "|-- " }) + $item.Name + "`n"
    if ($item.PSIsContainer) {
        $children = Get-ChildItem $path | Where-Object { $_.Name -notmatch '^(\.gradle|\.idea|build|\.kotlin|kspCaches|generated|intermediates|outputs|tmp|caches|captures|\.git|.*cache.*|.*\.temp|node_modules|dist|out)$' } | Sort-Object Name
        for ($i = 0; $i -lt $children.Count; $i++) {
            $isLastChild = ($i -eq $children.Count - 1)
            $result += Get-ProjectTree -path $children[$i].FullName -indent ($indent + $(if ($isLast) { "    " } else { "|   " })) -isLast $isLastChild
        }
    }
    return $result
}

function Summarize-Imports {
    param([string]$content)
    if (-not $summarizeImports) { return $content }
    if ($content -match '(?s)^((?:import\s+[^\r\n;]+[\r\n;]+)+)') {
        $importBlock = $matches[1]
        $importCount = ($importBlock -split "[\r\n;]" | Where-Object { $_.Trim() -match '^\s*import\s+' }).Count
        if ($importCount -gt 0) {
            $summary = "// {0} imports foram suprimidos para maior clareza.`n`n" -f $importCount
            $content = $summary + $content.Substring($importBlock.Length)
        }
    }
    return $content
}

function Save-GroupedFiles {
    param([array]$files, [string]$outputFile, [string]$groupName, [int]$maxFiles, [string]$projectName, [string]$timestamp)
    if (-not $files -or $files.Count -eq 0) { return }

    $totalFiles = $files.Count
    $totalParts = [Math]::Ceiling($totalFiles / $maxFiles)
    $basePath = [System.IO.Path]::GetDirectoryName($outputFile)
    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($outputFile)
    $extension = [System.IO.Path]::GetExtension($outputFile)

    for ($part = 1; $part -le $totalParts; $part++) {
        $partFileName = if ($totalParts -gt 1) { "{0}_{1}{2}" -f $baseName, $part, $extension } else { "{0}{1}" -f $baseName, $extension }
        $partOutputFile = Join-Path $basePath -ChildPath $partFileName
        $filesForPart = $files | Select-Object -Skip (($part - 1) * $maxFiles) -First $maxFiles

        $header = "# PROJETO: {0}`n# PACOTE: {1}`n# DATA: {2}`n# Arquivos incluidos: {3}`n`n" -f $projectName, $groupName, $timestamp, $filesForPart.Count
        $header | Out-File $partOutputFile -Encoding UTF8

        foreach ($pf in $filesForPart) {
            if (Test-Path $pf.TempPath) {
                $content = Get-Content $pf.TempPath -Raw -Encoding UTF8
                $fileHeader = "`n---`n## ARQUIVO: $($pf.RelPath)`n---`n"
                $fileExtension = [System.IO.Path]::GetExtension($pf.RelPath).TrimStart('.')
                # construir o bloco de código sem confundir o parser com triple-backticks
                $fileContent = '```' + $fileExtension + "`n" + $content + "`n" + '```'
                ($fileHeader + $fileContent) | Out-File $partOutputFile -Encoding UTF8 -Append
            } else {
                Write-Warning "Arquivo temporário não encontrado: $($pf.TempPath)"
            }
        }
    }
}

function Save-ListedFiles {
    param([array]$files, [string]$outputFile, [string]$groupName, [string]$projectName, [string]$timestamp)
    if (-not $files -or $files.Count -eq 0) { return }
    $header = "# PROJETO: {0}`n# PACOTE: {1}`n# DATA: {2}`n# Arquivos listados: {3}`n`n" -f $projectName, $groupName, $timestamp, $files.Count
    $header | Out-File $outputFile -Encoding UTF8
    ($files | ForEach-Object { "- " + $_.RelPath }) -join "`n" | Out-File $outputFile -Encoding UTF8 -Append
}

# Configurações
$maxFilesPerPart = 50
$excludeFolders = @('.gradle', '.idea', 'build', '.kotlin', 'kspCaches', 'generated', 'intermediates', 'outputs', 'tmp', 'caches', 'captures', '.git', 'temp_processed_files', 'node_modules', 'dist', 'out')
$textExtensions = @('*.kt', '*.java', '*.xml', '*.md', '*.gradle', '*.kts', '*.properties', '*.json', '*.yml', '*.yaml', '*.toml', '*.txt', '*.gitignore', '*.pro', '*.sql')
$nonTextExtensions = @('*.png', '*.jpg', '*.jpeg', '*.gif', '*.webp', '*.bmp', '*.ico', '*.svg', '*.jar', '*.aar', '*.zip', '*.keystore', '*.jks', '*.so', '*.ttf', '*.otf', '*.woff', '*.woff2')

# Valida paths básicos
if (-not (Test-Path $projectPath)) {
    Write-Error "Caminho do projeto não encontrado: $projectPath"
    return
}
if (-not (Test-Path $outputPath)) {
    New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
}

$projectName = (Get-Item $projectPath).Name
$timestamp = Get-Date -Format 'yyyy-MM-dd_HH-mm'
$projectOutDir = Join-Path $outputPath ("{0}_Consolidado_{1}" -f $projectName, $timestamp)
New-Item -ItemType Directory -Path $projectOutDir -Force | Out-Null

$tempPath = Join-Path $projectOutDir "temp_processed_files"
if (Test-Path $tempPath) { Remove-Item $tempPath -Recurse -Force }
New-Item -ItemType Directory -Path $tempPath | Out-Null

Write-Host "Analisando arquivos em '$projectPath'..." -ForegroundColor Yellow
$allFiles = Get-ChildItem $projectPath -Recurse -File | Sort-Object FullName
$filesToProcess = @()
$filesToList = @()

foreach ($f in $allFiles) {
    $relPath = $f.FullName.Substring($projectPath.Length + 1)
    if ($excludeFolders | Where-Object { $relPath -match ("(^|\\|/)" + [Regex]::Escape($_) + "($|\\|/)") }) { continue }
    if ($f.Length -gt 2MB) { continue }

    if ($textExtensions | Where-Object { $f.Name -like $_ }) {
        $filesToProcess += [PSCustomObject]@{ FullName = $f.FullName; RelPath = $relPath }
    } elseif ($nonTextExtensions | Where-Object { $f.Name -like $_ }) {
        $filesToList += [PSCustomObject]@{ FullName = $f.FullName; RelPath = $relPath }
    }
}

$processedFiles = @()
if ($includeReadme) {
    $readme = $filesToProcess | Where-Object { $_.RelPath -imatch '^README\.(md|txt)$' } | Select-Object -First 1
    if ($readme) {
        $tempFile = Join-Path $tempPath "000_README.tmp"
        (Get-Content $readme.FullName -Raw) | Out-File $tempFile -Encoding UTF8
        $processedFiles += [PSCustomObject]@{ TempPath = $tempFile; RelPath = $readme.RelPath }
        $filesToProcess = $filesToProcess | Where-Object { $_.FullName -ne $readme.FullName }
    }
}

$counter = 1
foreach ($file in $filesToProcess) {
    $newName = "{0:D3}_{1}.tmp" -f $counter, ($file.RelPath -replace '[\\/:]', '_')
    $tempFile = Join-Path $tempPath $newName
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        if ($content) {
            $content = Summarize-Imports -content $content
            $content | Out-File $tempFile -Encoding UTF8
            $processedFiles += [PSCustomObject]@{ TempPath = $tempFile; RelPath = $file.RelPath }
        }
    } catch {
        Write-Warning "Erro ao processar arquivo: $($file.RelPath) - $($_.Exception.Message)"
    }
    $counter++
}

# Arquivos de saída
$outputFull = Join-Path $projectOutDir "Project_Consolidated.md"
$outputMain = Join-Path $projectOutDir "App_Source.md"
$outputGradle = Join-Path $projectOutDir "Build_Config.md"
$outputTest = Join-Path $projectOutDir "Tests.md"
$outputBaseline = Join-Path $projectOutDir "Baseline.md"
$outputMisc = Join-Path $projectOutDir "Project_Misc.md"
$outputAssets = Join-Path $projectOutDir "Assets_Listing.md"
$outputTree = Join-Path $projectOutDir "Tree.md"

$gradleFiles = $processedFiles | Where-Object { $_.RelPath -match 'gradle|\.kts$|\.gradle$|\.toml$|\.properties$' }
$mainFiles = $processedFiles | Where-Object { ($_.RelPath -match 'src[\\/](main)[\\/]') -and ($gradleFiles -notcontains $_) }
$testFiles = $processedFiles | Where-Object { $_.RelPath -match 'src[\\/](test|androidTest)[\\/]' }
$baselineFiles = $processedFiles | Where-Object { $_.RelPath -match 'baseline-prof' }
$miscFiles = $processedFiles | Where-Object { ($gradleFiles -notcontains $_) -and ($mainFiles -notcontains $_) -and ($testFiles -notcontains $_) -and ($baselineFiles -notcontains $_) -and ($_.RelPath -notmatch '^README\.(md|txt)$') }

Write-Host "Gerando pacotes de consolidacao..." -ForegroundColor Yellow
Save-GroupedFiles -files $mainFiles -outputFile $outputMain -groupName "CODIGO FONTE (SRC/MAIN)" -maxFiles $maxFilesPerPart -projectName $projectName -timestamp $timestamp
Save-GroupedFiles -files $gradleFiles -outputFile $outputGradle -groupName "BUILD E CONFIGURACOES (GRADLE)" -maxFiles $maxFilesPerPart -projectName $projectName -timestamp $timestamp
Save-GroupedFiles -files $testFiles -outputFile $outputTest -groupName "TESTES (TEST + ANDROIDTEST)" -maxFiles $maxFilesPerPart -projectName $projectName -timestamp $timestamp
Save-GroupedFiles -files $baselineFiles -outputFile $outputBaseline -groupName "PERFIL DE BASE (BASELINE-PROFILE)" -maxFiles $maxFilesPerPart -projectName $projectName -timestamp $timestamp
Save-GroupedFiles -files $miscFiles -outputFile $outputMisc -groupName "ARQUIVOS DIVERSOS" -maxFiles $maxFilesPerPart -projectName $projectName -timestamp $timestamp
Save-ListedFiles -files $filesToList -outputFile $outputAssets -groupName "ASSETS E ARQUIVOS BINARIOS" -projectName $projectName -timestamp $timestamp

$treeContent = ""
if ($includeTree) {
    $treeContent = Get-ProjectTree -path $projectPath
    # montar string do tree sem confundir o parser
    $treeBlock = "# ESTRUTURA DO PROJETO`n`n" + '```text`n' + $treeContent + "`n" + '```'
    $treeBlock | Out-File $outputTree -Encoding UTF8
}

Write-Host "Gerando arquivo consolidado completo..." -ForegroundColor Yellow

# construir lista de grupos de forma explícita (evita ifs embutidos que podem confundir o parser)
$groupsToConsolidate = @()

# adicionar estrutura do projeto (se houver)
if ($includeTree) {
    $groupsToConsolidate += [PSCustomObject]@{ Name = "ESTRUTURA DO PROJETO"; Files = $null; Content = '```text`n' + $treeContent + "`n" + '```'; IsTree = $true }
}

$groupsToConsolidate += [PSCustomObject]@{ Name = "BUILD E CONFIGURACOES (GRADLE)"; Files = $gradleFiles; Content = $null; IsList = $false }
$groupsToConsolidate += [PSCustomObject]@{ Name = "CODIGO FONTE (SRC/MAIN)"; Files = $mainFiles; Content = $null; IsList = $false }
$groupsToConsolidate += [PSCustomObject]@{ Name = "TESTES (TEST + ANDROIDTEST)"; Files = $testFiles; Content = $null; IsList = $false }
$groupsToConsolidate += [PSCustomObject]@{ Name = "PERFIL DE BASE (BASELINE-PROFILE)"; Files = $baselineFiles; Content = $null; IsList = $false }
$groupsToConsolidate += [PSCustomObject]@{ Name = "ARQUIVOS DIVERSOS"; Files = $miscFiles; Content = $null; IsList = $false }
$groupsToConsolidate += [PSCustomObject]@{ Name = "ASSETS E ARQUIVOS BINARIOS"; Files = $filesToList; Content = $null; IsList = $true }

"# PROJETO COMPLETO CONSOLIDADO: {0}`n# DATA: {1}`n" -f $projectName, $timestamp | Out-File $outputFull -Encoding UTF8

foreach ($group in $groupsToConsolidate) {
    $hasFiles = ($group.Files -ne $null -and $group.Files.Count -gt 0)
    $hasContent = ($group.Content -ne $null -and $group.Content.Length -gt 0)
    if ($hasFiles -or $hasContent) {
        "`n## PACOTE: $($group.Name)`n" | Out-File $outputFull -Encoding UTF8 -Append

        if ($hasContent) {
            $group.Content | Out-File $outputFull -Encoding UTF8 -Append
        } elseif ($group.IsList) {
            "`n" + (($group.Files | ForEach-Object { "- " + $_.RelPath }) -join "`n") | Out-File $outputFull -Encoding UTF8 -Append
        } else {
            foreach ($pf in $group.Files) {
                if (Test-Path $pf.TempPath) {
                    $content = Get-Content $pf.TempPath -Raw -Encoding UTF8
                    $fileHeader = "`n---`n### ARQUIVO: $($pf.RelPath)`n---`n"
                    $fileExtension = [System.IO.Path]::GetExtension($pf.RelPath).TrimStart('.')
                    $fileContent = '```' + $fileExtension + "`n" + $content + "`n" + '```'
                    ($fileHeader + $fileContent) | Out-File $outputFull -Encoding UTF8 -Append
                } else {
                    Write-Warning "Arquivo temporário não encontrado: $($pf.TempPath)"
                }
            }
        }
    }
}

if (Test-Path $tempPath) { Remove-Item $tempPath -Recurse -Force }

Write-Host "Consolidacao concluida com sucesso!" -ForegroundColor Green
Write-Host "Saida salva em: $projectOutDir" -ForegroundColor Cyan
Write-Host "Gerados arquivos .md individuais e um consolidado geral." -ForegroundColor Yellow
Write-Host "Arquivos grandes podem ter sido divididos em partes." -ForegroundColor Yellow
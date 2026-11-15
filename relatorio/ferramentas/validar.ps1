$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$failures = [System.Collections.Generic.List[string]]::new()

foreach ($entry in Import-Csv -LiteralPath (Join-Path $root 'evidencias/sha256-origens.csv')) {
    $file = Join-Path $root $entry.path
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        $failures.Add("Arquivo ausente: $($entry.path)")
    } elseif ((Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() -ne $entry.sha256) {
        $failures.Add("Hash divergente: $($entry.path)")
    }
}

$main = Get-Content -LiteralPath (Join-Path $root 'main.tex') -Raw -Encoding UTF8
foreach ($section in @('introduction','theoretical_foundation','methodology','results','results_analysis','conclusion')) {
    if (-not $main.Contains("\input{sections/$section}")) { $failures.Add("Secao nao incluida: $section") }
}
foreach ($required in @('main.pdf','references.bib','sbc-template.sty','sbc.bst','appendices/source_code.tex')) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $required) -PathType Leaf)) { $failures.Add("Arquivo obrigatorio ausente: $required") }
}

$texFiles = @(Get-ChildItem -LiteralPath $root -Filter '*.tex' -File)
$texFiles += Get-ChildItem -LiteralPath (Join-Path $root 'sections'),(Join-Path $root 'tables'),(Join-Path $root 'appendices') -Filter '*.tex' -File
$content = ($texFiles | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 }) -join "`n"
$labels = @{}
foreach ($match in [regex]::Matches($content, '(?:\\label|label=)\{([^}]+)\}')) {
    $name = $match.Groups[1].Value
    if ($labels.ContainsKey($name)) { $failures.Add("Rotulo duplicado: $name") }
    $labels[$name] = $true
}
foreach ($match in [regex]::Matches($content, '\\(?:ref|eqref)\{([^}]+)\}')) {
    if (-not $labels.ContainsKey($match.Groups[1].Value)) { $failures.Add("Referencia sem rotulo: $($match.Groups[1].Value)") }
}
foreach ($match in [regex]::Matches($content, '\\(?:input|lstinputlisting)(?:\[[^\]]*\])?\{([^}]+)\}')) {
    $target = Join-Path $root $match.Groups[1].Value
    if (-not (Test-Path -LiteralPath $target) -and -not (Test-Path -LiteralPath ($target + '.tex'))) {
        $failures.Add("Dependencia local ausente: $($match.Groups[1].Value)")
    }
}

$logFile = Join-Path $root 'main.log'
if (Test-Path -LiteralPath $logFile) {
    $log = Get-Content -LiteralPath $logFile -Raw
    foreach ($pattern in @('undefined references','Citation .+ undefined','Reference .+ undefined',
            'Undefined control sequence','Missing character','Overfull','Label\(s\) may have changed','multiply defined')) {
        if ($log -match $pattern) { $failures.Add("Aviso/erro relevante no log: $pattern") }
    }
} else {
    Write-Output 'Log local ausente. Compile main.tex para conferir referencias e paginação.'
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Output "[FALHA] $_" }
    exit 1
}
Write-Output '[OK] Integridade das copias, estrutura, referencias locais e log LaTeX conferidos.'

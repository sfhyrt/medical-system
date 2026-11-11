param([switch]$SkipInstall)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Push-Location (Join-Path $projectRoot 'frontend')
try {
    if (!$SkipInstall) {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw 'npm ci failed' }
    }
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'Vue build failed' }
} finally { Pop-Location }
Push-Location $projectRoot
try {
    & mvn.cmd -B -ntp clean verify
    if ($LASTEXITCODE -ne 0) { throw 'Maven verification failed' }
    $jarEntries = & jar.exe tf (Join-Path $projectRoot 'target\medical-system-0.0.1-SNAPSHOT.jar')
    if ($LASTEXITCODE -ne 0 -or $jarEntries -notcontains 'BOOT-INF/classes/static/index.html') {
        throw 'The Jar does not contain the Vue page'
    }
    Write-Output 'Verified: Vue index.html is included in the executable Jar.'
} finally { Pop-Location }

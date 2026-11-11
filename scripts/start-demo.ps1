param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$projectJar = Join-Path $projectRoot 'target\medical-system-0.0.1-SNAPSHOT.jar'
if (!(Test-Path -LiteralPath $projectJar)) { throw 'Build first: .\scripts\build-demo.ps1' }
Push-Location $projectRoot
try { & java.exe -jar $projectJar "--server.port=$Port" }
finally { Pop-Location }

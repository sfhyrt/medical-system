param([int]$Port = 8080)
$ErrorActionPreference = 'Stop'
$labRoot = Split-Path -Parent $PSScriptRoot
$labJar = Join-Path $labRoot 'target\medical-system-0.0.1-SNAPSHOT.jar'
if (!(Test-Path -LiteralPath $labJar)) { throw 'Build first: .\scripts\build-demo.ps1' }
Push-Location $labRoot
try { & java.exe -jar $labJar "--server.port=$Port" }
finally { Pop-Location }

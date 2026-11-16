param([switch]$Initialize,[switch]$Verify)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
 if($Initialize){ & mvn.cmd -B -ntp -f tools/codegen/pom.xml compile exec:java '-Dexec.args=--initialize' }
 elseif($Verify) { & mvn.cmd -B -ntp -f tools/codegen/pom.xml compile exec:java '-Dexec.args=--verify-generation' }
 else { & mvn.cmd -B -ntp -f tools/codegen/pom.xml compile exec:java }
 if($LASTEXITCODE -ne 0){throw 'Persistence tooling failed'}
} finally {Pop-Location}

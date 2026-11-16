param([switch]$SampleData)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path -Parent $PSScriptRoot
Push-Location $projectRoot
try {
 & mvn.cmd -B -ntp -f tools/codegen/pom.xml compile exec:java '-Dexec.mainClass=com.example.medical.codegen.WorkflowMigration'
 if($LASTEXITCODE -ne 0){throw 'Database migration failed'}
 if($SampleData){
  & mvn.cmd -B -ntp -f tools/codegen/pom.xml exec:java '-Dexec.mainClass=com.example.medical.codegen.WorkflowMigration' '-Dexec.args=--sample'
  if($LASTEXITCODE -ne 0){throw 'Synthetic data initialization failed'}
 }
}
finally {Pop-Location}

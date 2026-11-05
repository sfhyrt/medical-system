param([string]$BaseUrl = 'http://127.0.0.1:8080')

$ErrorActionPreference = 'Stop'
$response = Invoke-WebRequest -Uri ($BaseUrl.TrimEnd('/') + '/api/hello') -UseBasicParsing -TimeoutSec 10
if ([int]$response.StatusCode -ne 200 -or [string]$response.Content -ne 'Hello World! Medical System') {
    throw 'Hello endpoint verification failed.'
}
[pscustomobject]@{
    Status = [int]$response.StatusCode
    Body = [string]$response.Content
    Result = 'PASS'
}

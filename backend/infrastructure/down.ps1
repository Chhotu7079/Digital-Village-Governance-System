param(
  [string]$EnvFile = ".env",
  [switch]$RemoveVolumes
)

$ErrorActionPreference = "Stop"

$volArg = ""
if ($RemoveVolumes) {
  $volArg = "-v"
}

Write-Host "Stopping DVGS infrastructure..." -ForegroundColor Cyan

docker compose --env-file $EnvFile down $volArg

Write-Host "Done." -ForegroundColor Green

param(
  [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $EnvFile)) {
  Write-Host "Env file '$EnvFile' not found. Copy .env.example to .env first." -ForegroundColor Yellow
  exit 1
}

Write-Host "Starting DVGS infrastructure using $EnvFile..." -ForegroundColor Cyan

docker compose --env-file $EnvFile up -d

Write-Host "Done." -ForegroundColor Green

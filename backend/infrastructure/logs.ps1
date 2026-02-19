param(
  [string]$EnvFile = ".env",
  [string]$Service = ""
)

$ErrorActionPreference = "Stop"

if ($Service -and $Service.Trim().Length -gt 0) {
  docker compose --env-file $EnvFile logs -f $Service
} else {
  docker compose --env-file $EnvFile logs -f
}

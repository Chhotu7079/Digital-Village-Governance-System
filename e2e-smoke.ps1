<#
DVGS E2E Smoke Test (PowerShell)

What it does:
1) Checks /actuator/health for gateway + services
2) Requests OTP
3) Prompts you to enter OTP received (from logs or WhatsApp)
4) Verifies OTP and extracts access token
5) Calls Land + Ration endpoints through API Gateway using Bearer token

Prerequisites:
- Infrastructure up: backend/infrastructure docker compose up -d
- All services running on default ports
- API Gateway running on 8080

Usage:
  powershell -ExecutionPolicy Bypass -File .\e2e-smoke.ps1

Optional environment variables:
  $env:DVGS_PHONE_NUMBER  (default: +919999999999)
  $env:DVGS_OTP_CHANNEL   (default: SMS)  # SMS or WHATSAPP
#>

$ErrorActionPreference = 'Stop'

function Write-Step($msg) {
  Write-Host "\n=== $msg ===" -ForegroundColor Cyan
}

function Invoke-JsonPost($url, $body, $headers = $null) {
  $json = $body | ConvertTo-Json -Depth 10
  if ($headers) {
    return Invoke-RestMethod -Method Post -Uri $url -ContentType 'application/json' -Headers $headers -Body $json
  }
  return Invoke-RestMethod -Method Post -Uri $url -ContentType 'application/json' -Body $json
}

function Invoke-Get($url, $headers = $null) {
  if ($headers) {
    return Invoke-RestMethod -Method Get -Uri $url -Headers $headers
  }
  return Invoke-RestMethod -Method Get -Uri $url
}

function Check-Health($name, $url) {
  try {
    $res = Invoke-Get $url
    $status = $res.status
    if ($status -ne 'UP') {
      throw "Health status not UP: $status"
    }
    Write-Host "[PASS] $name health UP ($url)" -ForegroundColor Green
  } catch {
    Write-Host "[FAIL] $name health check failed ($url) :: $($_.Exception.Message)" -ForegroundColor Red
    throw
  }
}

$phone = if ($env:DVGS_PHONE_NUMBER) { $env:DVGS_PHONE_NUMBER } else { '+919999999999' }
$channel = if ($env:DVGS_OTP_CHANNEL) { $env:DVGS_OTP_CHANNEL } else { 'SMS' }

$gatewayBase = 'http://localhost:8080'

Write-Step "1) Health checks"
Check-Health 'api-gateway' "$gatewayBase/actuator/health"
Check-Health 'auth-service' 'http://localhost:8081/actuator/health'
Check-Health 'complaint-service' 'http://localhost:8082/actuator/health'
Check-Health 'notification-service' 'http://localhost:8083/actuator/health'
Check-Health 'scheme-service' 'http://localhost:8084/actuator/health'
Check-Health 'land-service' 'http://localhost:8085/actuator/health'
Check-Health 'ration-service' 'http://localhost:8086/actuator/health'

Write-Step "2) Request OTP via Gateway (phone=$phone channel=$channel)"
$requestOtpUrl = "$gatewayBase/api/auth/login/otp/request"
Invoke-JsonPost $requestOtpUrl @{ phoneNumber = $phone; channel = $channel } | Out-Null
Write-Host "OTP requested. Check auth-service logs (SMS) or WhatsApp (if enabled)." -ForegroundColor Yellow

Write-Step "3) Enter OTP"
$otp = Read-Host 'Enter the OTP you received'

Write-Step "4) Verify OTP and extract access token"
$verifyUrl = "$gatewayBase/api/auth/login/otp/verify"
$loginResponse = Invoke-JsonPost $verifyUrl @{ phoneNumber = $phone; otpCode = $otp }

# Try common field names (depends on LoginResponse)
$accessToken = $null
if ($loginResponse.accessToken) { $accessToken = $loginResponse.accessToken }
elseif ($loginResponse.token -and $loginResponse.token.accessToken) { $accessToken = $loginResponse.token.accessToken }
elseif ($loginResponse.tokens -and $loginResponse.tokens.accessToken) { $accessToken = $loginResponse.tokens.accessToken }

if (-not $accessToken) {
  Write-Host "Could not find accessToken in response. Full response:" -ForegroundColor Red
  $loginResponse | ConvertTo-Json -Depth 20 | Write-Host
  throw "Login response did not include accessToken (adjust parsing in script)"
}

Write-Host "[PASS] Logged in. Access token acquired." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $accessToken" }

Write-Step "5) Land service call via Gateway"
$landUrl = "$gatewayBase/api/land/records/search?district=Patna&anchal=Danapur&mauza=Mauza-001&khataNo=15&khesraNo=221"
$landRes = Invoke-Get $landUrl $headers
$landRes | ConvertTo-Json -Depth 20 | Write-Host
Write-Host "[PASS] Land lookup succeeded" -ForegroundColor Green

Write-Step "6) Ration service call via Gateway"
$rationUrl = "$gatewayBase/api/ration/cards/BR-RC-0003"
$rationRes = Invoke-Get $rationUrl $headers
$rationRes | ConvertTo-Json -Depth 20 | Write-Host
Write-Host "[PASS] Ration lookup succeeded" -ForegroundColor Green

Write-Step "DONE"
Write-Host "E2E smoke completed successfully." -ForegroundColor Green

# Auth Service

## Purpose
The Auth Service provides authentication and authorization capabilities for the Digital Village Governance System. It manages OTP-based logins, JWT token issuance, refresh token flows, and role-based access control for citizens, officials, and administrators.

## Responsibilities
- User registration and identity proofing (citizen, official, admin)
- Mobile OTP verification and fallback password-based login for officials/admins
- JWT access + refresh token lifecycle management
- Role- and scope-based authorization rules
- Session revocation and device management
- Integration hooks for external identity providers (e.g., Aadhaar-based verification in future)

## Technology Stack
- Java 21 + Spring Boot
- Spring Security with OAuth2 Resource Server
- PostgreSQL/MySQL for user persistence
- Redis (optional) for token/session caching
- Twilio/WhatsApp Business API for OTP delivery

## Module Structure
```
src/
 ├─ main/
 │   ├─ java/com/dvgs/auth/
 │   │   ├─ controller
 │   │   ├─ service
 │   │   ├─ repository
 │   │   ├─ domain
 │   │   ├─ dto
 │   │   ├─ config
 │   │   ├─ security
 │   │   └─ exception
 │   └─ resources/
 │       ├─ application.yml
 │       └─ db/migration
 └─ test/
     └─ java/com/dvgs/auth/
```

## Key APIs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST   | `/api/auth/login/otp/request` | Request OTP for mobile number |
| POST   | `/api/auth/login/otp/verify` | Verify OTP and issue JWT tokens |
| POST   | `/api/auth/token/refresh` | Refresh access token |
| GET    | `/api/auth/me` | Fetch current user profile |

## OTP Flow
1. **Request OTP** – Citizen submits phone number and channel (SMS/WhatsApp). Service enforces cooldown and generates code.
2. **Send OTP** – Code is dispatched via configured gateways (console mock for non-prod environments).
3. **Verify OTP** – Citizen submits code; system validates expiry, attempt limits, and updates challenge status.
4. **Issue Tokens** – On success, JWT access token and refresh token (device scoped) are generated.
5. **Refresh Token** – Client exchanges refresh token for new access token when needed.

## Configuration
`application.yml` exposes `auth.token` and `auth.otp` sections for TTLs, cooldowns, lengths, and secrets. Use environment-specific overrides for production values.

## Next Steps
- Integrate actual SMS/WhatsApp providers
- Add password login flow for officials/admins
- Implement RBAC-enforced endpoints
- Add audit log persistence on OTP events/tokens

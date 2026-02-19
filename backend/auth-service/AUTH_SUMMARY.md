# Auth Service Progress Summary

_Last updated: current iteration_

## Platform Targets
- **Java version:** 21 (configured via `pom.xml`)  
- **Spring Boot:** 3.2.5 parent  
- **Dependency highlights:** Spring Web/Security/Data JPA, OAuth2 resource server, Redis starter placeholder, Twilio SDK, JJWT 0.12.5

## Domain & Persistence
- Core entities live under `com.dvgs.auth.domain`:
  - `User` (roles, status, audit timestamps via `AuditableEntity`).
  - `RoleType` enum (CITIZEN, OFFICIAL, ADMIN, SUPER_ADMIN).
  - `RefreshToken`, `OtpChallenge`, `AuthAuditLog`, all inheriting `AuditableEntity` timestamps.
- `AuditableEntity` auto-manages `created_at` / `updated_at`.
- Flyway migration `V1__init.sql` provisions tables for users, roles, refresh tokens, OTP challenges (with locked flag), and audit logs.

## Repositories
- Spring Data JPA repositories for `User`, `RefreshToken`, `OtpChallenge`, `AuthAuditLog` with helper queries (e.g., OTP request counting, device-specific refresh-token deletion).

## DTOs
- Request/response models cover registration, OTP request/verify, login response, refresh token flow, profile view, etc. (`com.dvgs.auth.dto`).

## Services & Business Logic
- `AuthService`: orchestrates OTP request/verify, updates last login, issues tokens, refresh flow, profile lookup, and logout (refresh-token revocation + device cleanup).
- `OtpService`: 
  - Generates OTPs, persists challenges, and routes through mock SMS/WhatsApp gateways.
  - Enforces **rate limiting** (3 requests / 10 min by default), resend cooldowns, and max attempt lockouts.
  - Locks OTPs after verification or repeated failures to prevent reuse.
- `TokenService`:
  - Generates JWT access tokens using JJWT 0.12.5 with non-deprecated APIs and centrally managed `SecretKey` (`JwtKeyConfig`).
  - Creates, validates, and revokes refresh tokens (device-aware deletion, explicit logout handling).
- `UserMapper`: centralizes mapping to DTOs such as login responses and profile views.

## Configuration & Security
- `AuthProperties` (loaded from `application.yml`) exposes OTP/token settings (TTL, issuer, rate-limit window, max attempts, secrets, etc.).
- `application.yml` includes datasource credentials, auth settings, and Flyway toggles.
- `SecurityConfig`: stateless JWT resource server, permits OTP & refresh endpoints, protects others.
- `JwtConfig` + `JwtKeyConfig`: configure `JwtDecoder` and `SecretKey` (supports Base64 secrets) for verifying/signing JWTs.

## Controllers & Error Handling
- `AuthController` exposes REST endpoints for OTP request/verify, token refresh, logout, and `/me` profile retrieval.
- Custom exceptions (`AuthException`, `OtpException`, `RefreshTokenException`) handled via `RestExceptionHandler` for consistent API errors.

## Utilities / Mock Integrations
- Console-based SMS and WhatsApp gateway implementations for dev/demo OTP delivery.

## Notable Security Enhancements
1. **OTP Request Rate Limiting:** per-phone windowed counter (default 3 per 10 minutes) to curb spam.
2. **Wrong Attempt Lockout:** OTP locked after 3 failed tries or expiration; must request new OTP.
3. **OTP Reuse Prevention:** verified OTPs marked locked & verified; subsequent use rejected.
4. **Logout Revocation:** `/api/auth/logout` revokes the provided refresh token and purges device tokens to block reuse.
5. **JWT Modernization:** uses Java 21 compatible APIs (non-deprecated JJWT methods) and centralized key management.

---
This file captures the current state of the Auth Service. Future updates (e.g., real messaging providers, audit logging expansion, integration tests) should append to this summary.

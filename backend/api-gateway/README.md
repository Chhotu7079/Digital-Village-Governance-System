# DVGS API Gateway

Spring Cloud Gateway for the **Digital Village Governance System**.

## What it does

- Routes traffic to backend microservices (auth/complaint/scheme/notification)
- Validates JWTs at the edge (HS256 shared-secret)
- Adds / propagates `X-Request-Id`
- Emits structured access logs (logger: `ACCESS_LOG`)
- Applies rate limiting for sensitive endpoints (Redis-backed, configurable fail-open)
- Returns consistent JSON error responses for common failures (401/403/404/429)

## Default ports

- **api-gateway**: `8080`
- **auth-service**: `8081`
- **complaint-service**: `8082`
- **notification-service**: `8083`
- **scheme-service**: `8084`

## Required env vars

### JWT
Auth-service signs JWTs using **HS256**.

You must set the same secret in the gateway:

- `AUTH_JWT_SECRET` (required)
  - mapped to `spring.security.oauth2.resourceserver.jwt.secret`

Optional:
- `AUTH_JWT_ISSUER` (default: `dvgs-auth`)

### Redis (rate limiting)
Rate limiting requires Redis.

- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)

Fail-open toggle:
- `GATEWAY_RATELIMIT_FAIL_OPEN` (default: `true`)
  - `true`: Redis/limiter failures do **not** block requests
  - `false`: Redis/limiter failures will fail requests

### Service URLs (optional overrides)
- `AUTH_SERVICE_URL` (default: `http://localhost:8081`)
- `COMPLAINT_SERVICE_URL` (default: `http://localhost:8082`)
- `NOTIFICATION_SERVICE_URL` (default: `http://localhost:8083`)
- `SCHEME_SERVICE_URL` (default: `http://localhost:8084`)

### CORS
- `GATEWAY_CORS_ALLOWED_ORIGINS`
  - default: `http://localhost:3000,http://localhost:5173`

## Rate limiting defaults

Configured in `src/main/resources/application.yml`:

- `/api/auth/**`
  - `RL_AUTH_REPLENISH_RATE` (default: `5`)
  - `RL_AUTH_BURST_CAPACITY` (default: `10`)

- `/api/attachments/**`
  - `RL_ATTACH_REPLENISH_RATE` (default: `10`)
  - `RL_ATTACH_BURST_CAPACITY` (default: `20`)

## Actuator exposure (safe default)

By default, only exposes:
- `health,info`

Override with:
- `ACTUATOR_EXPOSE=health,info,metrics,prometheus`

## Run locally

From `backend/api-gateway`:

```bash
mvn spring-boot:run
```

Example (Windows CMD):

```bat
set AUTH_JWT_SECRET=your-secret
set REDIS_HOST=localhost
set REDIS_PORT=6379
mvn spring-boot:run
```

## Smoke tests

```bash
mvn test
```

These tests do **not** require downstream services to be running.

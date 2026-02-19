# DVGS Land Service

Read-only service that **fetches Bihar-style land record details from an external “existence API”**.

Today it points to our **mock external API** (in this repo). Later you can swap the base URL to a real government API.

## What this service does

- Exposes DVGS endpoint:
  - `GET /api/land/records/search?district=&anchal=&mauza=&khataNo=&khesraNo=`
- Calls external API:
  - `GET {LAND_EXTERNAL_BASE_URL}/api/external/land/records/search?...`
- Enforces JWT authentication (HS256 shared secret)
- Privacy defaults:
  - **CITIZEN** gets only: `{ exists, district, anchal, mauza, khataNo, khesraNo, area, unit, ownerCount }`
  - **OFFICIAL/ADMIN/SUPER_ADMIN** gets full details (jamabandiNo + owner list)
- Caches external responses in Redis (TTL configurable)
- Logs and exposes Prometheus-ready metrics for external API calls

## Default ports

- land-service: `8085`
- mock external land API: `8092`

## Required environment variables

### JWT
- `AUTH_JWT_SECRET` **(required)**
- `AUTH_JWT_ISSUER` (default: `dvgs-auth`)

### External API
- `LAND_EXTERNAL_BASE_URL` (default: `http://localhost:8092`)
- `LAND_EXTERNAL_TIMEOUT_MS` (default: `3000`)

### Redis cache
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `LAND_CACHE_ENABLED` (default: `true`)
- `LAND_CACHE_TTL_SECONDS` (default: `300`)

### Actuator exposure
- `ACTUATOR_EXPOSE` (default: `health,info`)

## Run locally

From `backend/land-service`:

```bat
set AUTH_JWT_SECRET=your-secret
set LAND_EXTERNAL_BASE_URL=http://localhost:8092
set REDIS_HOST=localhost
set REDIS_PORT=6379
mvn spring-boot:run
```

## Swagger

- http://localhost:8085/swagger-ui.html

# DVGS Ration Service

Read-only service that **fetches ration card details from an external “existence API”**.

Today it points to our **mock external API** (in this repo). Later you can swap the base URL to a real government API.

## What this service does

- Exposes DVGS endpoint: `GET /api/ration/cards/{cardNo}`
- Calls external API: `GET {RATION_EXTERNAL_BASE_URL}/api/external/ration/cards/{cardNo}`
- Enforces JWT authentication (HS256 shared secret)
- Strong privacy defaults:
  - **CITIZEN** gets only: `{ exists, cardType, status, memberCount }`
  - **OFFICIAL/ADMIN/SUPER_ADMIN** gets full details (location + members)
- Caches external responses in Redis (TTL configurable)
- Logs and exposes Prometheus metrics for external API calls

## Default port

- `8086`

## Required environment variables

### JWT
- `AUTH_JWT_SECRET` **(required)**
- `AUTH_JWT_ISSUER` (default: `dvgs-auth`)

### External API
- `RATION_EXTERNAL_BASE_URL` (default: `http://localhost:8091`)
- `RATION_EXTERNAL_TIMEOUT_MS` (default: `3000`)

### Redis cache
- `REDIS_HOST` (default: `localhost`)
- `REDIS_PORT` (default: `6379`)
- `RATION_CACHE_ENABLED` (default: `true`)
- `RATION_CACHE_TTL_SECONDS` (default: `300`)

### Actuator exposure
- `ACTUATOR_EXPOSE` (default: `health,info,prometheus`)

## Run locally

From `backend/ration-service`:

### Windows (CMD)

```bat
set AUTH_JWT_SECRET=your-secret
set RATION_EXTERNAL_BASE_URL=http://localhost:8091
set REDIS_HOST=localhost
set REDIS_PORT=6379
mvn spring-boot:run
```

## API

### GET /api/ration/cards/{cardNo}

Example:

```
GET /api/ration/cards/BR-RC-0003
Authorization: Bearer <jwt>
```

Citizen response example:

```json
{
  "exists": true,
  "cardNo": "BR-RC-0003",
  "cardType": "PHH",
  "status": "ACTIVE",
  "memberCount": 6,
  "location": null,
  "members": []
}
```

Official/Admin response includes `location` + `members`.

Not found (Citizen):

```json
{ "exists": false, "cardNo": "BR-RC-9999" }
```

Not found (Official/Admin): HTTP 404

## Swagger

- http://localhost:8086/swagger-ui.html

## Monitoring

### Actuator
- http://localhost:8086/actuator/health
- http://localhost:8086/actuator/prometheus

### External call metrics

- Timer: `ration.external.http.client{method,path,status}`
- Counter: `ration.external.http.client.count{method,path,status}`

## Gateway route

api-gateway routes:
- `/api/ration/**` → `RATION_SERVICE_URL` (default: `http://localhost:8086`)

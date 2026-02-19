# Mock Ration Existence API

This is a **mock external API** used by DVGS `ration-service` during development.

It simulates a government ration-card lookup API.

## Default port

- `8091`

## Run

From `backend/mock-services/ration-existence-api`:

```bash
mvn spring-boot:run
```

## Endpoint

### GET /api/external/ration/cards/{cardNo}

Example:

```http
GET http://localhost:8091/api/external/ration/cards/BR-RC-0003
```

Returns 200 with ration card details, or 404 if not found.

## Sample card numbers

Seeded in `RationCardController`:

- `BR-RC-0001` (3 members)
- `BR-RC-0002` (2 members)
- `BR-RC-0003` (6 members)
- `BR-RC-0004` (5 members)

## Swagger

- http://localhost:8091/swagger-ui.html

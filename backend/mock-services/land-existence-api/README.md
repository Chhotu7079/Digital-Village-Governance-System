# Mock Land Existence API

Mock external API used by DVGS `land-service` during development.

## Default port
- `8092`

## Run

```bash
mvn spring-boot:run
```

## Endpoint

### GET /api/external/land/records/search

Required query params (Bihar-style):
- `district`
- `anchal`
- `mauza`
- `khataNo`
- `khesraNo`

Example:

```http
GET http://localhost:8092/api/external/land/records/search?district=Patna&anchal=Danapur&mauza=Mauza-001&khataNo=15&khesraNo=221
```

## Seeded sample records

1) Patna / Danapur / Mauza-001 / khata 15 / khesra 221
2) Nalanda / Biharsharif / Mauza-021 / khata 7 / khesra 45
3) Gaya / Tekari / Mauza-009 / khata 3 / khesra 11

## Swagger
- http://localhost:8092/swagger-ui.html

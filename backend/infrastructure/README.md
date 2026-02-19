# DVGS Infrastructure (Docker Compose)

This folder provides a **single docker-compose** to run the shared infrastructure needed by DVGS services.

## Includes

- PostgreSQL (single instance) + init script to create service databases
- Redis (for API Gateway rate limiting)
- Kafka + Zookeeper (scheme-service events to notification-service)
- Kafka topic auto-init (`kafka-init`) for `notification-events`
- RabbitMQ (notification-service)
- MinIO (scheme-service attachments)

## Quick start

From `backend/infrastructure`:

### Option A: helper scripts (Windows PowerShell)

```powershell
./up.ps1
./logs.ps1
./down.ps1
```

### Option B: docker compose directly

```bash
docker compose --env-file .env up -d
```

If you don’t have a `.env` yet:

```bash
copy .env.example .env
```

Then run compose.

## Service DBs created

On first run Postgres will create:
- `dvgs_auth`
- `dvgs_complaint`
- `dvgs_scheme`
- `dvgs_notification`

Credentials (defaults):
- user: `dvgs`
- pass: `dvgs`

## Useful URLs

- Postgres: `localhost:5432`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
  - Topic auto-created: `notification-events` (override: `KAFKA_TOPIC_NOTIFICATION_EVENTS`)
- RabbitMQ management UI: `http://localhost:15672` (guest/guest)
- MinIO console: `http://localhost:9001` (minioadmin/minioadmin)

## Notes

- MinIO bucket auto-init: `minio-init` ensures the bucket exists on startup.
  - Default bucket: `dvgs-attachments`
  - Override with: `MINIO_BUCKET_ATTACHMENTS`

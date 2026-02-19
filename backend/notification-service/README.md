# Notification Service

Spring Boot microservice responsible for orchestrating outbound notifications (SMS, WhatsApp, push, email) for the Digital Village Governance System.

## Features
- Template-based message composition with localization
- REST + event-driven triggers via Kafka/RabbitMQ
- Channel adapters (SMS via Twilio, WhatsApp Business via Twilio, email via SES, push via Firebase) with fallback routing and DND enforcement
- Notification preference management with role-aware access controls
- Delivery tracking with NotificationLog + provider callbacks
- Retry scheduling hooks and delivery event publishing for analytics

## Tech Stack
- Spring Boot 3.2.5, Java 21
- Spring Web, Validation, Data JPA, Security (OAuth2 Resource Server)
- Kafka & RabbitMQ integration
- PostgreSQL + Flyway migrations
- MapStruct & Lombok for DTO mapping
- Micrometer Prometheus registry + SpringDoc OpenAPI

## APIs (high-level)
- `POST /api/notifications` – submit notification requests (admin/officer only)
- `GET /api/notifications/{id}` – retrieve per-channel status
- `GET/POST/DELETE /api/templates` – template management (admin/officer)
- `GET/PATCH /api/preferences/{userId}` – manage channel preferences (citizen self-service)
- `GET /api/analytics/notifications` – aggregated channel stats (admin/officer)
- `/api/callbacks/*` – provider delivery callbacks

## Observability & Analytics
- Micrometer metrics (Prometheus) for submissions, dispatch outcomes (per channel/status), latency, skipped channels, and status lookups.
- Delivery events published to Kafka (`notification-delivery-events`).
- Role-protected analytics endpoint returning aggregated NotificationLog counts by channel/status.

## Running Locally
```bash
cd backend/notification-service
mvn spring-boot:run
```

Configure DB, messaging endpoints, and provider credentials via `src/main/resources/application.yml` or environment variables (e.g., `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `FIREBASE_SERVICE_ACCOUNT_JSON`).

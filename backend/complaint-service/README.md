# Complaint Service

Spring Boot microservice (Java 21) for complaint management in the Digital Village Governance System.

## Stack
- Spring Boot 3.2.5
- Java 21 (LTS)
- Spring Web, Validation, Data JPA, OAuth2 Resource Server
- PostgreSQL + Flyway for persistence
- MapStruct + Lombok for mappings/boilerplate reduction

## Getting Started
```bash
cd backend/complaint-service
mvn clean install
```

## Configuration
See `src/main/resources/application.yml` for datasource and service config. Update DB credentials before running.
- `auth.token.*` — JWT secret/issuer (shared with Auth Service)
- `attachments.*` — local storage path, size/type limits, download-token secret/TTL
- `notification-service.url` — endpoint for forwarding complaint events
- `management.endpoints.web.exposure.include` — expose health/info/metrics/prometheus as needed
- `kafka.*` — Kafka bootstrap servers & topic for complaint events
- `complaint.sla.*` — scheduling + escalation delays for auto-SLA monitoring

## Analytics
Exposes `/api/analytics/status`, `/api/analytics/departments`, and `/api/analytics/sla` for admin/officer dashboards (status counts, department workload, SLA compliance).

## Notification UX Enhancements
- Citizens can manage notification preferences (`/api/notifications/preferences`).
- Templates (`notification-templates.yml`) support SMS/WhatsApp bodies in multiple languages.
- Event publisher renders localized templates, honors user preferences, and skips disabled users.

## Next Steps
- Build richer notification templates/localization and delivery tracking atop the Kafka event stream
- Harden multi-level analytics/metrics on top of the existing pagination + SLA/audit features
- Expand test coverage (unit + integration) and observability hooks

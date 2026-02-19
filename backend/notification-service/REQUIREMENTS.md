# Notification Service – Requirements Structure

## 1. Purpose & Scope
Provide a centralized service for generating and delivering DVGS notifications (SMS, WhatsApp, in-app push, email) across all modules (complaints, schemes, land, ration, announcements) with online/offline resilience.

## 2. Stakeholders & Consumers
- **Citizen App / Portal** – receive status updates, reminders, announcements.
- **Official Dashboards** – acknowledgments, escalations, SLA breaches.
- **Microservices** – complaint-service, scheme-service, land-service, ration-service, announcement-service, auth-service.
- **External Channels** – SMS gateway, WhatsApp Business API, email provider (optional), push notification broker.

## 3. Functional Requirements
1. **Notification Composition**
   - Template management with localization (multi-language support).
   - Placeholder resolution from payload/context (e.g., citizen name, complaint ID).
   - Priority & category tagging (informational, critical, SLA, reminder).
2. **Channel Dispatching**
   - SMS via configured provider (e.g., Twilio).
   - WhatsApp messages with media support for rich updates.
   - In-app push/web notifications via WebSocket/Firebase.
   - Email (optional, pluggable provider).
3. **Triggering Mechanisms**
   - Event-driven via message queue (e.g., Kafka/RabbitMQ) from upstream services.
   - Direct REST API for synchronous triggers (admin announcements, manual alerts).
   - Scheduled/recurring notifications (ration distribution reminders, follow-ups).
4. **Delivery Tracking**
   - Persist NotificationLog entries with status (queued, sent, delivered, failed, retried).
   - Webhook/callback handling from providers to update delivery status.
   - Retry strategy with exponential backoff for transient failures.
5. **Preference & Opt-in Management**
   - Respect user channel preferences and DND windows.
   - Handle fallback hierarchy (e.g., WhatsApp → SMS → voice) when channel unavailable.
6. **Offline/Low-Bandwidth Support**
   - Queue notifications for later sync when citizen device offline (hand-off to mobile app sync engine).
   - Lightweight payload options for constrained channels (USSD/SMS summaries).

## 4. Non-Functional Requirements
- **Scalability:** Horizontally scalable microservice, stateless workers, queue-backed throughput.
- **Reliability:** Retry policies, dead-letter queues, circuit breakers for provider outages.
- **Security:** JWT-authenticated APIs, encrypted secrets for provider credentials, audit logging.
- **Observability:** Structured logs, metrics (delivery latency, success rate), tracing across services.
- **Localization:** Unicode-safe templates, RTL support, dynamic language selection per user profile.
- **Compliance:** Consent tracking, GDPR-like data deletion hooks, message throttling to avoid spam.

## 5. Integrations & Dependencies
| Source Service | Event Examples | Required Data |
| -------------- | -------------- | ------------- |
| complaint-service | complaint_created, status_updated, feedback_request | citizen contact info, complaint ID, next action |
| scheme-service | application_submitted, approved, rejected | scheme name, benefit details |
| land-service | dispute_filed, mutation_approved | parcel ID, parties involved |
| ration-service | stock_update, distribution_day | ration card number, schedule |
| announcement-service | new_announcement, emergency_alert | geo targets, languages |
| auth-service | otp_generated, password_reset | contact channel, OTP |

External providers: Twilio SMS, WhatsApp Business API, optional SMTP/email, push broker (FCM/WebPush), future USSD/voice interfaces.

## 6. Data Model (Draft)
- **NotificationTemplate**: id, name, version, language, channel, body, placeholders, metadata.
- **NotificationRequest**: requestId, sourceService, payload, preferredChannels, priority.
- **NotificationLog**: logId, requestId, channel, status, attemptCount, errorCode, timestamps.
- **UserChannelPreference**: userId, channel, enabled, dndWindow, fallbackChannel.
- **ProviderCredential** (secure storage): providerId, channel, apiKey/secret, status.

## 7. API Surface (Initial)
1. `POST /notifications` – submit notification request.
2. `GET /notifications/{id}` – fetch consolidated status.
3. `GET /templates` / `POST /templates` – manage templates (admin only).
4. `PATCH /preferences/{userId}` – update channel preferences.
5. Provider webhooks (e.g., `/callbacks/twilio`, `/callbacks/whatsapp`).

## 8. Message Flow
1. Upstream service emits domain event → notification-service consumes (async) or calls REST (sync).
2. Service enriches context (user profile lookup, localization) via common-lib.
3. Selects template & channel mix based on event, user preferences, urgency.
4. Dispatches to channel adapters (SMS/WhatsApp/push/email) via provider SDKs or HTTP APIs.
5. Logs status, updates NotificationLog, triggers retries or fallback channels if failures occur.
6. Emits delivery events for analytics dashboards and SLA monitoring.

## 9. Configuration & Secrets
- Provider credentials in Vault/Secrets Manager (env-specific).
- Channel enable/disable toggles per environment.
- Rate limits per channel, per user.
- Retry/backoff, DLQ thresholds, template cache expiry.

## 10. Testing & Validation
- Unit tests for template rendering, payload validation.
- Contract tests for REST APIs and event schemas per upstream service.
- Integration tests with sandbox providers (Twilio test credentials).
- Load tests for burst dispatch (e.g., emergency alerts to entire village).

## 11. Open Questions / Follow-ups
- Final choice of message queue and push provider.
- Need for multilingual text-to-speech/voice calls?
- Storage location for large media attachments (reuse S3 bucket?).
- Governance of template versions and approvals.

---
This structure captures the high-level requirements for the Notification Service and will guide subsequent design and implementation steps.
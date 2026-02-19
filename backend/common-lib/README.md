# DVGS common-lib

This module contains **shared contracts and small utilities** that are reused across DVGS microservices.

## Keep here
- Cross-service **event contracts** (Kafka/Rabbit payload DTOs) that are produced by one service and consumed by another.
- Small **request context** utilities (e.g. request-id header constants).
- Small **security helpers** (claim/role extraction helpers) that do not hard-couple services.
- Common API error model (only if standardized across services).

## Do NOT keep here
- JPA entities / repositories
- Service business logic
- Service-specific REST DTOs
- Spring `@Configuration` that differs per service

## Versioning
This library should be versioned carefully because changes can affect multiple services.

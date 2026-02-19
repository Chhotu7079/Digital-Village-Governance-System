package com.dvgs.scheme.dto;

import java.time.Instant;

public class AuditDtos {

    public record AuditLogResponse(
            Long id,
            String entityType,
            String entityId,
            String action,
            String actorUserId,
            String message,
            String metadata,
            Instant createdAt
    ) {}
}

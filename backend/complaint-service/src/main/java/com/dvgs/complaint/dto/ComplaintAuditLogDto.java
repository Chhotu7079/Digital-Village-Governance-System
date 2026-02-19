package com.dvgs.complaint.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintAuditLogDto {
    UUID id;
    UUID complaintId;
    UUID actorId;
    String action;
    String details;
    Instant createdAt;
}

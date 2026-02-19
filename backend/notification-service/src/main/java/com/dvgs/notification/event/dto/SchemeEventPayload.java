package com.dvgs.notification.event.dto;

import java.time.Instant;
import lombok.Data;

/**
 * Payload emitted by scheme-service onto Kafka topic `notification-events`.
 *
 * NOTE: This mirrors scheme-service's SchemeEvent structure.
 */
@Data
public class SchemeEventPayload {
    private Long applicationId;
    private Long schemeId;
    private String applicantUserId;
    private String assignedOfficerId;
    private String status;
    private String type;
    private Instant timestamp;
    private String description;
}

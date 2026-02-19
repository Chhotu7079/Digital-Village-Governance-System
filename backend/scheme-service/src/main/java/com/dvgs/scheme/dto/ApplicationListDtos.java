package com.dvgs.scheme.dto;

import com.dvgs.scheme.domain.ApplicationStatus;
import java.time.Instant;

public class ApplicationListDtos {

    public record ApplicationListItem(
            Long id,
            Long schemeId,
            String applicantUserId,
            ApplicationStatus status,
            String assignedOfficerId,
            Instant submittedAt,
            Instant updatedAt,
            Instant currentStatusSince
    ) {}
}

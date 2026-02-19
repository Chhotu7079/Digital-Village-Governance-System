package com.dvgs.scheme.event;

import com.dvgs.scheme.domain.ApplicationStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SchemeEvent {
    Long applicationId;
    Long schemeId;
    String applicantUserId;
    String assignedOfficerId;
    ApplicationStatus status;
    String type;
    Instant timestamp;
    String description;

}

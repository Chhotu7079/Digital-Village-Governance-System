package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintPriority;
import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintSummary {
    UUID id;
    String title;
    ComplaintPriority priority;
    ComplaintStatus status;
    UUID departmentId;
    UUID assignedOfficerId;
    Instant createdAt;
    Instant updatedAt;
    Set<String> tags;
}

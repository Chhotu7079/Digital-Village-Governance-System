package com.dvgs.complaint.event;

import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintEvent {
    UUID complaintId;
    UUID citizenId;
    UUID departmentId;
    UUID assignedOfficerId;
    ComplaintStatus status;
    String type;
    Instant timestamp;
    String description;
}

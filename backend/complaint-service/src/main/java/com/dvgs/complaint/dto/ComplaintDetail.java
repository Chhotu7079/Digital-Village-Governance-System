package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintChannel;
import com.dvgs.complaint.domain.ComplaintPriority;
import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintDetail {
    UUID id;
    String title;
    String description;
    UUID citizenId;
    UUID departmentId;
    UUID assignedOfficerId;
    ComplaintPriority priority;
    ComplaintStatus status;
    ComplaintChannel channel;
    Instant expectedResolutionAt;
    Instant closedAt;
    Set<String> tags;
    List<ComplaintAttachmentDto> attachments;
    List<ComplaintStatusHistoryDto> history;
}

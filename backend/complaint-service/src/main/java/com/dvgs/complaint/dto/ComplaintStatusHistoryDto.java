package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintStatusHistoryDto {
    UUID id;
    ComplaintStatus fromStatus;
    ComplaintStatus toStatus;
    UUID changedBy;
    String remarks;
    Instant statusChangedAt;
}

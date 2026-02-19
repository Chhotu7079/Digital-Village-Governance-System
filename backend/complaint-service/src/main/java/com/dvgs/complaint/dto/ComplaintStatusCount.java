package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintStatusCount {
    ComplaintStatus status;
    long count;
}

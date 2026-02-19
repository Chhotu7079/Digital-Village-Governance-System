package com.dvgs.complaint.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ComplaintFeedbackDto {
    UUID id;
    UUID citizenId;
    int rating;
    String comments;
    Instant createdAt;
}

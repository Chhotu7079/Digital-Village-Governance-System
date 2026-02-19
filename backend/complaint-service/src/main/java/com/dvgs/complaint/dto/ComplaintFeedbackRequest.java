package com.dvgs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

@Data
public class ComplaintFeedbackRequest {

    @JsonIgnore
    private UUID complaintId;

    @JsonIgnore
    private UUID citizenId;

    @Min(1)
    @Max(5)
    private int rating;

    @Size(max = 2000)
    private String comments;
}

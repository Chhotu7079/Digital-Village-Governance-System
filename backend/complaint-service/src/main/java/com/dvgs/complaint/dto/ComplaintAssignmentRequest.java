package com.dvgs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class ComplaintAssignmentRequest {

    @NotNull
    private UUID officerId;

    @JsonIgnore
    private UUID assignedBy;
}

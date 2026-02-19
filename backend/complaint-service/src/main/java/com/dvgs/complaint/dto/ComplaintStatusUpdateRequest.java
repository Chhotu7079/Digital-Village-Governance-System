package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Data;

@Data
public class ComplaintStatusUpdateRequest {

    @NotNull
    private ComplaintStatus toStatus;

    @JsonIgnore
    private UUID changedBy;

    @Size(max = 1000)
    private String remarks;
}

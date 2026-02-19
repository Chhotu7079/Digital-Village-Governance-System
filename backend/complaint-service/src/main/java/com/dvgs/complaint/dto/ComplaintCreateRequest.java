package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.ComplaintChannel;
import com.dvgs.complaint.domain.ComplaintPriority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class ComplaintCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 4000)
    private String description;

    @JsonIgnore
    private UUID citizenId;

    @NotNull
    private UUID departmentId;

    @NotNull
    private ComplaintPriority priority;

    @NotNull
    private ComplaintChannel channel;

    private Set<String> tags;
}

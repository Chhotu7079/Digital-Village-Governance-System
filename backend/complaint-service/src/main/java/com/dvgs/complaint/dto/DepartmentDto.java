package com.dvgs.complaint.dto;

import com.dvgs.complaint.domain.DepartmentCategory;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DepartmentDto {
    UUID id;
    String name;
    String description;
    DepartmentCategory category;
    Integer slaHours;
    UUID leadOfficerId;
}

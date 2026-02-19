package com.dvgs.complaint.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DepartmentAnalytics {
    UUID departmentId;
    String departmentName;
    long openComplaints;
    long escalatedComplaints;
    long resolvedComplaints;
}

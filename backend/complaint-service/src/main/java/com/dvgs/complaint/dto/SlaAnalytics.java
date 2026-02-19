package com.dvgs.complaint.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SlaAnalytics {
    long totalComplaints;
    long withinSla;
    long breachedSla;
    double breachPercentage;
}

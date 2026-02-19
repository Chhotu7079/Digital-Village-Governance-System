package com.dvgs.scheme.dto;

import com.dvgs.scheme.domain.ApplicationStatus;
import java.util.Map;

public class AnalyticsDtos {

    public record StatusCount(ApplicationStatus status, long count) {}

    public record SchemeStatusCount(Long schemeId, ApplicationStatus status, long count) {}

    public record AgingBuckets(
            ApplicationStatus status,
            Map<String, Long> buckets
    ) {}

    public record TopScheme(Long schemeId, long applications) {}
}

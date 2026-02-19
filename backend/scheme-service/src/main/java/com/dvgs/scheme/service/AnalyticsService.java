package com.dvgs.scheme.service;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.AnalyticsDtos;
import com.dvgs.scheme.repository.SchemeApplicationRepository;
import com.dvgs.scheme.repository.SchemeRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SchemeApplicationRepository applicationRepository;
    private final SchemeRepository schemeRepository;

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.StatusCount> statusCounts() {
        List<AnalyticsDtos.StatusCount> out = new ArrayList<>();
        for (Object[] row : applicationRepository.countByStatus()) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            long count = ((Number) row[1]).longValue();
            out.add(new AnalyticsDtos.StatusCount(status, count));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.SchemeStatusCount> schemeStatusCounts() {
        List<AnalyticsDtos.SchemeStatusCount> out = new ArrayList<>();
        for (Object[] row : applicationRepository.countBySchemeAndStatusForSchemeStatus(SchemeStatus.ACTIVE)) {
            Long schemeId = (Long) row[0];
            ApplicationStatus status = (ApplicationStatus) row[1];
            long count = ((Number) row[2]).longValue();
            out.add(new AnalyticsDtos.SchemeStatusCount(schemeId, status, count));
        }
        return out;
    }

    /**
     * Returns bucket counts by how long an application has been in its current status.
     * Uses status history (latest changedAt) for SLA aging.
     * Buckets: >1d, >3d, >7d, >14d.
     */
    @Transactional(readOnly = true)
    public AnalyticsDtos.AgingBuckets aging(ApplicationStatus status) {
        Instant now = Instant.now();
        var buckets = new LinkedHashMap<String, Long>();

        buckets.put(">1d", applicationRepository.countOlderThanByHistory(status, now.minus(Duration.ofDays(1))));
        buckets.put(">3d", applicationRepository.countOlderThanByHistory(status, now.minus(Duration.ofDays(3))));
        buckets.put(">7d", applicationRepository.countOlderThanByHistory(status, now.minus(Duration.ofDays(7))));
        buckets.put(">14d", applicationRepository.countOlderThanByHistory(status, now.minus(Duration.ofDays(14))));

        return new AnalyticsDtos.AgingBuckets(status, buckets);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.TopScheme> topSchemes(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        Set<Long> activeIds = schemeRepository.findByStatus(SchemeStatus.ACTIVE).stream().map(s -> s.getId()).collect(java.util.stream.Collectors.toSet());

        List<AnalyticsDtos.TopScheme> out = new ArrayList<>();
        for (Object[] row : applicationRepository.topSchemes(org.springframework.data.domain.PageRequest.of(0, safeLimit))) {
            Long schemeId = (Long) row[0];
            if (!activeIds.contains(schemeId)) {
                continue;
            }
            long cnt = ((Number) row[1]).longValue();
            out.add(new AnalyticsDtos.TopScheme(schemeId, cnt));
        }
        return out;
    }
}

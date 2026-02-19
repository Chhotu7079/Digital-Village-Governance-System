package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.ComplaintStatus;
import com.dvgs.complaint.dto.ComplaintStatusCount;
import com.dvgs.complaint.dto.DepartmentAnalytics;
import com.dvgs.complaint.dto.SlaAnalytics;
import com.dvgs.complaint.repository.ComplaintRepository;
import com.dvgs.complaint.service.AnalyticsService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ComplaintRepository complaintRepository;

    @Override
    public List<ComplaintStatusCount> statusCounts() {
        return complaintRepository.countByStatus().stream()
                .map(row -> ComplaintStatusCount.builder()
                        .status((ComplaintStatus) row[0])
                        .count((long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentAnalytics> departmentAnalytics() {
        return complaintRepository.departmentStats().stream()
                .map(row -> DepartmentAnalytics.builder()
                        .departmentId((java.util.UUID) row[0])
                        .departmentName((String) row[1])
                        .openComplaints(((Number) row[2]).longValue())
                        .escalatedComplaints(((Number) row[3]).longValue())
                        .resolvedComplaints(((Number) row[4]).longValue())
                        .build())
                .toList();
    }

    @Override
    public SlaAnalytics slaAnalytics() {
        Object[] stats = complaintRepository.slaStats();
        long total = stats[0] == null ? 0 : ((Number) stats[0]).longValue();
        long withinSla = stats[1] == null ? 0 : ((Number) stats[1]).longValue();
        long breached = Math.max(0, total - withinSla);
        double percent = total == 0 ? 0 : (breached * 100.0) / total;
        return SlaAnalytics.builder()
                .totalComplaints(total)
                .withinSla(withinSla)
                .breachedSla(breached)
                .breachPercentage(percent)
                .build();
    }
}

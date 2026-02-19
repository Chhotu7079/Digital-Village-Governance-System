package com.dvgs.complaint.controller;

import com.dvgs.complaint.dto.ComplaintStatusCount;
import com.dvgs.complaint.dto.DepartmentAnalytics;
import com.dvgs.complaint.dto.SlaAnalytics;
import com.dvgs.complaint.service.AnalyticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','OFFICIAL')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/status")
    public ResponseEntity<List<ComplaintStatusCount>> statusCounts() {
        return ResponseEntity.ok(analyticsService.statusCounts());
    }

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentAnalytics>> departmentStats() {
        return ResponseEntity.ok(analyticsService.departmentAnalytics());
    }

    @GetMapping("/sla")
    public ResponseEntity<SlaAnalytics> slaMetrics() {
        return ResponseEntity.ok(analyticsService.slaAnalytics());
    }
}

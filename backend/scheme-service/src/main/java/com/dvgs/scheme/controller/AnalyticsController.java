package com.dvgs.scheme.controller;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.dto.AnalyticsDtos;
import com.dvgs.scheme.dto.ApplicationListDtos;
import com.dvgs.scheme.service.AnalyticsService;
import com.dvgs.scheme.service.ApplicationQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OFFICIAL','ADMIN','SUPER_ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ApplicationQueryService applicationQueryService;

    @GetMapping("/status")
    public List<AnalyticsDtos.StatusCount> statusCounts() {
        return analyticsService.statusCounts();
    }

    @GetMapping("/schemes")
    public List<AnalyticsDtos.SchemeStatusCount> schemeStatusCounts() {
        // aggregated; pagination not necessary here (small result set)
        return analyticsService.schemeStatusCounts();
    }

    @GetMapping("/top-schemes")
    public List<AnalyticsDtos.TopScheme> topSchemes(@RequestParam(defaultValue = "10") int limit) {
        return analyticsService.topSchemes(limit);
    }

    @GetMapping("/aging")
    public AnalyticsDtos.AgingBuckets aging(@RequestParam ApplicationStatus status) {
        return analyticsService.aging(status);
    }

    /**
     * Detail list with pagination for dashboards.
     */
    @GetMapping("/applications")
    public org.springframework.data.domain.Page<ApplicationListDtos.ApplicationListItem> applications(
            @RequestParam ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return applicationQueryService.listByStatus(status, page, size);
    }
}

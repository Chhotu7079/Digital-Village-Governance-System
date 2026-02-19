package com.dvgs.notification.controller;

import com.dvgs.notification.dto.NotificationChannelStatsDto;
import com.dvgs.notification.service.NotificationAnalyticsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics/notifications")
public class AnalyticsController {

    private final NotificationAnalyticsService analyticsService;

    public AnalyticsController(NotificationAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<List<NotificationChannelStatsDto>> getChannelStats() {
        return ResponseEntity.ok(analyticsService.getChannelStats());
    }
}

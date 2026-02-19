package com.dvgs.notification.service;

import com.dvgs.notification.dto.NotificationChannelStatsDto;
import java.util.List;

public interface NotificationAnalyticsService {
    List<NotificationChannelStatsDto> getChannelStats();
}

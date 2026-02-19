package com.dvgs.notification.service.impl;

import com.dvgs.notification.dto.NotificationChannelStatsDto;
import com.dvgs.notification.repository.NotificationLogRepository;
import com.dvgs.notification.service.NotificationAnalyticsService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationAnalyticsServiceImpl implements NotificationAnalyticsService {

    private final NotificationLogRepository logRepository;

    public NotificationAnalyticsServiceImpl(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Override
    public List<NotificationChannelStatsDto> getChannelStats() {
        return logRepository.aggregateChannelStats().stream()
                .map(view -> new NotificationChannelStatsDto(
                        view.getChannel(),
                        view.getStatus(),
                        view.getTotal()))
                .toList();
    }
}

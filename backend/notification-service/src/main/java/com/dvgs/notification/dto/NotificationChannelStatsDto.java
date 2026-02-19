package com.dvgs.notification.dto;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationStatus;

public record NotificationChannelStatsDto(ChannelType channel,
                                          NotificationStatus status,
                                          long total) {
}

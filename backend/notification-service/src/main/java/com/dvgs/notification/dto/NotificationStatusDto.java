package com.dvgs.notification.dto;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationStatus;
import java.time.OffsetDateTime;

public record NotificationStatusDto(
        ChannelType channel,
        NotificationStatus status,
        int attemptCount,
        String providerMessageId,
        OffsetDateTime lastAttemptAt
) { }

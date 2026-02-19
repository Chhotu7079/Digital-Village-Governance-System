package com.dvgs.notification.event.dto;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationPriority;
import java.util.List;
import java.util.Map;

public record NotificationEventPayload(
        String referenceId,
        String sourceService,
        NotificationPriority priority,
        List<ChannelType> preferredChannels,
        String templateCode,
        String language,
        Map<String, String> placeholders,
        String userId
) {}

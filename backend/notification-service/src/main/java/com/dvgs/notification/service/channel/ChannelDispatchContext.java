package com.dvgs.notification.service.channel;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.dto.NotificationRequestDto;
import java.util.Map;

import com.dvgs.notification.config.properties.NotificationProperties.ChannelSetting;

public record ChannelDispatchContext(
        String requestId,
        ChannelType channel,
        NotificationRequestDto request,
        String title,
        String body,
        Map<String, String> metadata,
        ChannelSetting channelSetting,
        String destination
) {
}

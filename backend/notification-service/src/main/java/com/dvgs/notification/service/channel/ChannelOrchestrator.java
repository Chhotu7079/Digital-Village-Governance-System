package com.dvgs.notification.service.channel;

import com.dvgs.notification.dto.NotificationRequestDto;

public interface ChannelOrchestrator {
    void process(NotificationRequestDto requestDto, String requestId);
}

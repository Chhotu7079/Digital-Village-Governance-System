package com.dvgs.notification.service;

import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.dto.NotificationStatusDto;
import java.util.List;

public interface NotificationDispatchService {
    String enqueueNotification(NotificationRequestDto requestDto);
    List<NotificationStatusDto> getStatus(String requestId);
}

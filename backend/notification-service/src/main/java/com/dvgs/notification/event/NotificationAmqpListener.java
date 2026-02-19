package com.dvgs.notification.event;

import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.service.NotificationDispatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationAmqpListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationAmqpListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationDispatchService dispatchService;

    public NotificationAmqpListener(ObjectMapper objectMapper,
                                    NotificationDispatchService dispatchService) {
        this.objectMapper = objectMapper;
        this.dispatchService = dispatchService;
    }

    @RabbitListener(queues = "notification-queue")
    public void handleMessage(String message) {
        try {
            NotificationRequestDto dto = objectMapper.readValue(message, NotificationRequestDto.class);
            dispatchService.enqueueNotification(dto);
        } catch (Exception ex) {
            log.error("Failed to process AMQP notification message", ex);
        }
    }
}

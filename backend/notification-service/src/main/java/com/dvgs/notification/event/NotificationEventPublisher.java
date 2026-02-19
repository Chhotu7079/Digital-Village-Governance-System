package com.dvgs.notification.event;

import com.dvgs.notification.domain.NotificationLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                      ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDeliveryEvent(NotificationLog logEntry) {
        try {
            String payload = objectMapper.writeValueAsString(logEntry);
            kafkaTemplate.send("notification-delivery-events", logEntry.getRequestId(), payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize notification log {}", logEntry.getId(), e);
        }
    }
}

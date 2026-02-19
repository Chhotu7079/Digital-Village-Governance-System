package com.dvgs.notification.event;

import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.event.dto.SchemeEventPayload;
import com.dvgs.notification.service.NotificationDispatchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationDispatchService dispatchService;
    private final SchemeEventToNotificationMapper schemeEventToNotificationMapper;

    public NotificationEventListener(ObjectMapper objectMapper,
                                     NotificationDispatchService dispatchService,
                                     SchemeEventToNotificationMapper schemeEventToNotificationMapper) {
        this.objectMapper = objectMapper;
        this.dispatchService = dispatchService;
        this.schemeEventToNotificationMapper = schemeEventToNotificationMapper;
    }

    @KafkaListener(topics = "notification-events")
    public void handleNotificationEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            // If payload contains "templateCode" we treat it as NotificationRequestDto (existing behavior)
            if (node.hasNonNull("templateCode")) {
                NotificationRequestDto dto = objectMapper.treeToValue(node, NotificationRequestDto.class);
                dispatchService.enqueueNotification(dto);
                return;
            }

            // If payload contains "applicationId" and "type" we treat it as SchemeEventPayload
            if (node.hasNonNull("applicationId") && node.hasNonNull("type")) {
                SchemeEventPayload schemeEvent = objectMapper.treeToValue(node, SchemeEventPayload.class);
                log.info("Received scheme event type={} applicationId={} schemeId={} applicantUserId={}",
                        schemeEvent.getType(),
                        schemeEvent.getApplicationId(),
                        schemeEvent.getSchemeId(),
                        schemeEvent.getApplicantUserId());

                NotificationRequestDto mapped = schemeEventToNotificationMapper.map(schemeEvent);
                if (mapped == null) {
                    log.info("No notification template mapping for scheme event type={}", schemeEvent.getType());
                    return;
                }

                dispatchService.enqueueNotification(mapped);
                return;
            }

            log.warn("Unknown notification-events payload shape: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse notification event payload: {}", payload, e);
        }
    }
}

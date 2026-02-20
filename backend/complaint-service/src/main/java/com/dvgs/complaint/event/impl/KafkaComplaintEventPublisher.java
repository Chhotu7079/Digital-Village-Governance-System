package com.dvgs.complaint.event.impl;

import com.dvgs.complaint.config.kafka.KafkaProperties;
import com.dvgs.complaint.event.ComplaintEvent;
import com.dvgs.complaint.event.ComplaintEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class KafkaComplaintEventPublisher implements ComplaintEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Override
    public void publish(ComplaintEvent event) {
        try {
            kafkaTemplate.send(kafkaProperties.getComplaintTopic(), event.getComplaintId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send complaint event", ex);
                        } else {
                            log.info("Sent complaint event {}", event.getType());
                        }
                    });
        } catch (Exception ex) {
            log.error("Kafka publish error", ex);
        }
    }
}

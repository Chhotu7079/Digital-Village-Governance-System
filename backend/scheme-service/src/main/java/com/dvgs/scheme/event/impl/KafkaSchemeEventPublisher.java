package com.dvgs.scheme.event.impl;

import com.dvgs.scheme.config.kafka.KafkaProperties;
import com.dvgs.scheme.event.SchemeEvent;
import com.dvgs.scheme.event.SchemeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class KafkaSchemeEventPublisher implements SchemeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    @Override
    public void publish(SchemeEvent event) {
        try {
            String key = event.getApplicationId() != null ? event.getApplicationId().toString() : "";
            kafkaTemplate.send(kafkaProperties.getSchemeTopic(), key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send scheme event", ex);
                        } else {
                            log.info("Sent scheme event {}", event.getType());
                        }
                    });
        } catch (Exception ex) {
            log.error("Kafka publish error", ex);
        }
    }
}

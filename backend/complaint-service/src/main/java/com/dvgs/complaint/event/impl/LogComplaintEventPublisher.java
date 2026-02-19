package com.dvgs.complaint.event.impl;

import com.dvgs.complaint.event.ComplaintEvent;
import com.dvgs.complaint.event.ComplaintEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogComplaintEventPublisher implements ComplaintEventPublisher {

    @Override
    public void publish(ComplaintEvent event) {
        // Placeholder for real message broker (Kafka, RabbitMQ, etc.)
        log.info("Complaint event published: {}", event);
    }
}

package com.dvgs.complaint.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ComplaintMetrics {

    private final Counter complaintsCreated;
    private final Counter complaintsEscalated;
    private final Counter complaintsResolved;

    public ComplaintMetrics(MeterRegistry registry) {
        this.complaintsCreated = registry.counter("complaints.created");
        this.complaintsEscalated = registry.counter("complaints.escalated");
        this.complaintsResolved = registry.counter("complaints.resolved");
    }

    public void incrementCreated() {
        complaintsCreated.increment();
    }

    public void incrementEscalated() {
        complaintsEscalated.increment();
    }

    public void incrementResolved() {
        complaintsResolved.increment();
    }
}

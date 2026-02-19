package com.dvgs.notification.metrics;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationStatus;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NotificationMetricsPublisher {

    private final MeterRegistry meterRegistry;

    public NotificationMetricsPublisher(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSubmission(String sourceService, String priority) {
        meterRegistry.counter("notification.request.submitted",
                "source", sourceService,
                "priority", priority).increment();
    }

    public void recordStatusLookup() {
        meterRegistry.counter("notification.request.status.lookups").increment();
    }

    public void recordDispatchOutcome(String requestId,
                                      ChannelType channel,
                                      NotificationStatus status,
                                      OffsetDateTime createdAt,
                                      OffsetDateTime lastAttemptAt) {
        List<Tag> tags = List.of(
                Tag.of("channel", channel.name()),
                Tag.of("status", status.name())
        );
        meterRegistry.counter("notification.dispatch.count", tags).increment();
        if (createdAt != null && lastAttemptAt != null) {
            Duration latency = Duration.between(createdAt, lastAttemptAt);
            meterRegistry.timer("notification.dispatch.latency", tags)
                    .record(latency);
        }
    }

    public void recordPreferenceSkip(ChannelType channel, String reason) {
        meterRegistry.counter("notification.channel.skipped",
                "channel", channel.name(),
                "reason", reason).increment();
    }
}

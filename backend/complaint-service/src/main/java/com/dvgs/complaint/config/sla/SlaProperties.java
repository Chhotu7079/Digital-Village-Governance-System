package com.dvgs.complaint.config.sla;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "complaint.sla")
public class SlaProperties {

    private Duration checkInterval = Duration.ofMinutes(15);
    private Duration escalationDelay = Duration.ZERO;

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Duration getEscalationDelay() {
        return escalationDelay;
    }

    public void setEscalationDelay(Duration escalationDelay) {
        this.escalationDelay = escalationDelay;
    }
}

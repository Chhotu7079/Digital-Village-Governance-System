package com.dvgs.scheme.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scheme.application")
public class SchemeApplicationProperties {

    /**
     * Cooldown period (in days) before a citizen can re-apply to the same scheme after rejection.
     */
    private int reapplyCooldownDays = 30;

    public int getReapplyCooldownDays() {
        return reapplyCooldownDays;
    }

    public void setReapplyCooldownDays(int reapplyCooldownDays) {
        this.reapplyCooldownDays = reapplyCooldownDays;
    }
}

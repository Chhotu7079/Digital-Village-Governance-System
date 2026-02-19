package com.dvgs.notification.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class NotificationHealthIndicator implements HealthIndicator {

    @Value("${notification.channels.sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${notification.channels.whatsapp.enabled:true}")
    private boolean whatsappEnabled;

    @Value("${notification.channels.push.enabled:true}")
    private boolean pushEnabled;

    @Value("${notification.channels.email.enabled:false}")
    private boolean emailEnabled;

    @Override
    public Health health() {
        return Health.up()
                .withDetail("channels", Map.of(
                        "sms", smsEnabled,
                        "whatsapp", whatsappEnabled,
                        "push", pushEnabled,
                        "email", emailEnabled
                ))
                .build();
    }
}

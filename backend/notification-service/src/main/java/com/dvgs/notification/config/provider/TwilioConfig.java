package com.dvgs.notification.config.provider;

import com.dvgs.notification.config.properties.NotificationProperties;
import com.dvgs.notification.domain.ChannelType;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    /**
     * Twilio is only required when SMS (or WhatsApp) channels are enabled.
     * In core/local mode you can disable these channels in application.yml.
     */
    @Bean
    @ConditionalOnProperty(prefix = "notification.channels.sms", name = "enabled", havingValue = "true")
    public TwilioRestClient twilioRestClient(NotificationProperties properties) {
        NotificationProperties.ChannelSetting sms = properties.getChannelSetting(ChannelType.SMS);

        // If credentials are missing, fail fast with a clear message.
        if (sms.getAccountSid() == null || sms.getAccountSid().isBlank()
                || sms.getAuthToken() == null || sms.getAuthToken().isBlank()) {
            throw new IllegalStateException(
                    "Twilio credentials are missing. Set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN or disable notification.channels.sms.enabled");
        }

        Twilio.init(sms.getAccountSid(), sms.getAuthToken());
        return Twilio.getRestClient();
    }
}

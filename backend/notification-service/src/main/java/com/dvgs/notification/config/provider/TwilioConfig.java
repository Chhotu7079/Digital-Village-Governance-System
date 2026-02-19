package com.dvgs.notification.config.provider;

import com.dvgs.notification.config.properties.NotificationProperties;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Bean
    public TwilioRestClient twilioRestClient(NotificationProperties properties) {
        NotificationProperties.ChannelSetting sms = properties.getChannelSetting(com.dvgs.notification.domain.ChannelType.SMS);
        if (sms.getAccountSid() != null && sms.getAuthToken() != null) {
            Twilio.init(sms.getAccountSid(), sms.getAuthToken());
        }
        return Twilio.getRestClient();
    }
}

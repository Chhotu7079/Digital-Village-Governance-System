package com.dvgs.auth.service.twilio;

import com.dvgs.auth.service.OtpService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Sends OTP via Twilio WhatsApp (Sandbox or production WhatsApp Business).
 *
 * Enable with:
 *   - twilio.whatsapp-enabled=true
 * Provide credentials via:
 *   - twilio.account-sid
 *   - twilio.auth-token
 * Optional:
 *   - twilio.whatsapp-from (default: whatsapp:+14155238886)
 */
@Slf4j
@Component
@EnableConfigurationProperties(TwilioProperties.class)
@ConditionalOnProperty(prefix = "twilio", name = "whatsapp-enabled", havingValue = "true")
public class TwilioWhatsAppGateway implements OtpService.WhatsAppGateway {

    private final TwilioProperties props;

    public TwilioWhatsAppGateway(TwilioProperties props) {
        this.props = props;
    }

    @Override
    public void sendOtp(String phoneNumber, String code) {
        if (!props.isWhatsappEnabled()) {
            // keep local dev experience intact
            log.info("[WhatsApp OTP - disabled] {} -> {}", code, phoneNumber);
            return;
        }

        if (!StringUtils.hasText(props.getAccountSid()) || !StringUtils.hasText(props.getAuthToken())) {
            throw new IllegalStateException(
                    "Twilio WhatsApp is enabled but credentials are missing. "
                            + "Set twilio.account-sid and twilio.auth-token (env: TWILIO_ACCOUNT_SID/TWILIO_AUTH_TOKEN)."
            );
        }

        String to = normalizeToWhatsApp(phoneNumber);
        String from = props.getWhatsappFrom();

        Twilio.init(props.getAccountSid(), props.getAuthToken());

        // Keep message simple; you can localize later.
        String body = "Your DVGS OTP is: " + code;

        Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(from), body).create();
        log.info("[WhatsApp OTP] sent via Twilio sid={} to={}", message.getSid(), to);
    }

    private String normalizeToWhatsApp(String phoneNumber) {
        String trimmed = phoneNumber == null ? "" : phoneNumber.trim();
        if (trimmed.startsWith("whatsapp:")) {
            return trimmed;
        }
        // Expect E.164 like +91XXXXXXXXXX. If caller passes digits only, Twilio may reject.
        return "whatsapp:" + trimmed;
    }
}

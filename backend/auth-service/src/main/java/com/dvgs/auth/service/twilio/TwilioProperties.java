package com.dvgs.auth.service.twilio;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Twilio configuration for OTP delivery.
 *
 * IMPORTANT: Do not hardcode secrets. Provide values via environment variables or external config.
 */
@ConfigurationProperties(prefix = "twilio")
public class TwilioProperties {

    /** Enable/disable Twilio-based WhatsApp sending. */
    private boolean whatsappEnabled = false;

    /** Twilio Account SID (e.g. AC...). */
    private String accountSid;

    /** Twilio Auth Token. */
    private String authToken;

    /** WhatsApp sender, usually sandbox: whatsapp:+14155238886 */
    private String whatsappFrom = "whatsapp:+14155238886";

    public boolean isWhatsappEnabled() {
        return whatsappEnabled;
    }

    public void setWhatsappEnabled(boolean whatsappEnabled) {
        this.whatsappEnabled = whatsappEnabled;
    }

    public String getAccountSid() {
        return accountSid;
    }

    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getWhatsappFrom() {
        return whatsappFrom;
    }

    public void setWhatsappFrom(String whatsappFrom) {
        this.whatsappFrom = whatsappFrom;
    }
}

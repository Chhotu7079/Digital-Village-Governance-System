package com.dvgs.auth.service.mock;

import com.dvgs.auth.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
public class WhatsAppConsoleGateway implements OtpService.WhatsAppGateway {

    @Override
    public void sendOtp(String phoneNumber, String code) {
        log.info("[WhatsApp OTP] {} -> {}", code, phoneNumber);
    }
}

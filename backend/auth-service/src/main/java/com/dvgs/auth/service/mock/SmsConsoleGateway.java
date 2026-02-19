package com.dvgs.auth.service.mock;

import com.dvgs.auth.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
public class SmsConsoleGateway implements OtpService.SmsGateway {

    @Override
    public void sendOtp(String phoneNumber, String code) {
        log.info("[SMS OTP] {} -> {}", code, phoneNumber);
    }
}

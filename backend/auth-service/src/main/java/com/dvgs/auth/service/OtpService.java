package com.dvgs.auth.service;

import com.dvgs.auth.config.AuthProperties;
import com.dvgs.auth.domain.OtpChallenge;
import com.dvgs.auth.domain.OtpChallenge.OtpChannel;
import com.dvgs.auth.domain.User;
import com.dvgs.auth.dto.OtpRequest;
import com.dvgs.auth.dto.OtpVerifyRequest;
import com.dvgs.auth.exception.OtpException;
import com.dvgs.auth.repository.OtpChallengeRepository;
import com.dvgs.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpChallengeRepository otpChallengeRepository;
    private final UserRepository userRepository;
    private final AuthProperties authProperties;
    private final SmsGateway smsGateway;
    private final WhatsAppGateway whatsAppGateway;

    @Transactional
    public void requestOtp(OtpRequest request) {
        Instant now = Instant.now();
        OtpChallenge latest = otpChallengeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber())
                .orElse(null);
        if (latest != null && !latest.isVerified()) {
            Duration sinceLast = Duration.between(latest.getCreatedAt(), now);
            if (sinceLast.compareTo(authProperties.getOtp().getResendCooldown()) < 0) {
                throw new OtpException("Please wait before requesting another OTP");
            }
        }

        Instant windowStart = now.minus(authProperties.getOtp().getRequestWindow());
        long recentRequests = otpChallengeRepository.countByPhoneNumberAndCreatedAtAfter(
                request.getPhoneNumber(), windowStart);
        if (recentRequests >= authProperties.getOtp().getMaxRequestsPerWindow()) {
            throw new OtpException("Maximum OTP requests reached. Please try again later");
        }

        String code = generateCode(authProperties.getOtp().getCodeLength());
        Instant expiresAt = now.plus(authProperties.getOtp().getTtl());

        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("Citizen")
                        .phoneNumber(request.getPhoneNumber())
                        .status(User.UserStatus.ACTIVE)
                        .build()));

        OtpChallenge challenge = OtpChallenge.builder()
                .user(user)
                .phoneNumber(request.getPhoneNumber())
                .otpCode(code)
                .channel(request.getChannel())
                .expiresAt(expiresAt)
                .attempts(0)
                .locked(false)
                .verified(false)
                .build();
        otpChallengeRepository.save(challenge);

        sendCode(request.getPhoneNumber(), code, request.getChannel());
    }

    @Transactional
    public User verifyOtp(OtpVerifyRequest request) {
        OtpChallenge challenge = otpChallengeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber())
                .orElseThrow(() -> new OtpException("No OTP request found"));

        if (challenge.isVerified()) {
            throw new OtpException("OTP already used");
        }
        if (challenge.isLocked()) {
            throw new OtpException("Too many attempts. Request a new OTP");
        }
        if (Instant.now().isAfter(challenge.getExpiresAt())) {
            challenge.setLocked(true);
            otpChallengeRepository.save(challenge);
            throw new OtpException("OTP expired");
        }
        if (challenge.getAttempts() >= authProperties.getOtp().getMaxAttempts()) {
            challenge.setLocked(true);
            otpChallengeRepository.save(challenge);
            throw new OtpException("Maximum attempts exceeded");
        }
        if (!challenge.getOtpCode().equals(request.getOtpCode())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            otpChallengeRepository.save(challenge);
            throw new OtpException("Invalid OTP");
        }

        challenge.setVerified(true);
        challenge.setLocked(true);
        otpChallengeRepository.save(challenge);

        return userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private void sendCode(String phoneNumber, String code, OtpChannel channel) {
        if (channel == OtpChannel.WHATSAPP) {
            whatsAppGateway.sendOtp(phoneNumber, code);
        } else {
            smsGateway.sendOtp(phoneNumber, code);
        }
    }

    private String generateCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public interface SmsGateway {
        void sendOtp(String phoneNumber, String code);
    }

    public interface WhatsAppGateway {
        void sendOtp(String phoneNumber, String code);
    }
}

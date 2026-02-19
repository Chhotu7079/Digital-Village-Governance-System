package com.dvgs.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private final Token token = new Token();
    private final Otp otp = new Otp();

    public Token getToken() {
        return token;
    }

    public Otp getOtp() {
        return otp;
    }

    public static class Token {
        /**
         * Shared secret for signing JWTs (HMAC SHA256).
         */
        private String secret = "change-me";
        private Duration accessTtl = Duration.ofMinutes(15);
        private Duration refreshTtl = Duration.ofDays(30);
        private String issuer = "dvgs-auth";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessTtl() {
            return accessTtl;
        }

        public void setAccessTtl(Duration accessTtl) {
            this.accessTtl = accessTtl;
        }

        public Duration getRefreshTtl() {
            return refreshTtl;
        }

        public void setRefreshTtl(Duration refreshTtl) {
            this.refreshTtl = refreshTtl;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    public static class Otp {
        private int codeLength = 6;
        private Duration ttl = Duration.ofMinutes(5);
        private Duration resendCooldown = Duration.ofSeconds(45);
        private int maxAttempts = 3;
        private int maxRequestsPerWindow = 3;
        private Duration requestWindow = Duration.ofMinutes(10);

        public int getCodeLength() {
            return codeLength;
        }

        public void setCodeLength(int codeLength) {
            this.codeLength = codeLength;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public Duration getResendCooldown() {
            return resendCooldown;
        }

        public void setResendCooldown(Duration resendCooldown) {
            this.resendCooldown = resendCooldown;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public int getMaxRequestsPerWindow() {
            return maxRequestsPerWindow;
        }

        public void setMaxRequestsPerWindow(int maxRequestsPerWindow) {
            this.maxRequestsPerWindow = maxRequestsPerWindow;
        }

        public Duration getRequestWindow() {
            return requestWindow;
        }

        public void setRequestWindow(Duration requestWindow) {
            this.requestWindow = requestWindow;
        }
    }
}

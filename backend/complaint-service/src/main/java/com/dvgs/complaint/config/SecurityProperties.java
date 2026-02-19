package com.dvgs.complaint.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
public class SecurityProperties {

    private final Token token = new Token();

    public Token getToken() {
        return token;
    }

    public static class Token {
        private String secret = "change-me";
        private String issuer = "dvgs-auth";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }
}

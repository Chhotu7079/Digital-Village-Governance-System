package com.dvgs.notification.config;

import com.dvgs.notification.config.properties.AuthClientProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtDecoderConfig {

    private final AuthClientProperties authClientProperties;

    public JwtDecoderConfig(AuthClientProperties authClientProperties) {
        this.authClientProperties = authClientProperties;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        if (authClientProperties.getJwkSetUri() != null && !authClientProperties.getJwkSetUri().isBlank()) {
            return NimbusJwtDecoder.withJwkSetUri(authClientProperties.getJwkSetUri()).build();
        }
        String secret = authClientProperties.getSharedSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Either auth-client.jwk-set-uri or auth-client.shared-secret must be configured");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes();
        }
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}

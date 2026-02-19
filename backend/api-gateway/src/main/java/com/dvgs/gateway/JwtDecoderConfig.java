package com.dvgs.gateway;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.util.StringUtils;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret,
            @Value("${auth.jwt.issuer:dvgs-auth}") String issuer
    ) {
        if (!StringUtils.hasText(secret) || "change-me".equals(secret)) {
            throw new IllegalStateException("AUTH_JWT_SECRET not configured (spring.security.oauth2.resourceserver.jwt.secret)");
        }

        // HS256 secret key (same shared secret as auth-service)
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        OAuth2TokenValidator<Jwt> issuerValidator = new JwtClaimValidator<>("iss", iss -> issuer.equals(String.valueOf(iss)));
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, issuerValidator));
        return decoder;
    }
}

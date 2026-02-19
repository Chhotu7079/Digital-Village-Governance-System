package com.dvgs.ration;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.secret}") String secret,
            @Value("${auth.jwt.issuer:dvgs-auth}") String expectedIssuer
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("AUTH_JWT_SECRET is required for ration-service");
        }

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();

        return token -> decoder.decode(token)
                .flatMap(jwt -> validateIssuer(jwt, expectedIssuer));
    }

    private Mono<Jwt> validateIssuer(Jwt jwt, String expectedIssuer) {
        Object iss = jwt.getClaims().get(JwtClaimNames.ISS);
        if (iss == null || !expectedIssuer.equals(iss.toString())) {
            return Mono.error(new IllegalArgumentException("Invalid token issuer"));
        }
        return Mono.just(jwt);
    }
}

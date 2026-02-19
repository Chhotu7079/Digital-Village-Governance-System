package com.dvgs.complaint.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtKeyConfig {

    @Bean
    public SecretKey jwtSigningKey(SecurityProperties properties) {
        String secret = properties.getToken().getSecret();
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}

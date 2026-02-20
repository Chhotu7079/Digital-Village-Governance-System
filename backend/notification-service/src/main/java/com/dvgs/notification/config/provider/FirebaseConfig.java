package com.dvgs.notification.config.provider;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    /**
     * Firebase is only required when PUSH notifications are enabled.
     * For local/core-service runs we keep it disabled by default.
     */
    @Bean
    @ConditionalOnProperty(name = "FIREBASE_SERVICE_ACCOUNT_JSON")
    public FirebaseApp firebaseApp(@Value("${FIREBASE_SERVICE_ACCOUNT_JSON}") String serviceAccountJson) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(
                        new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))))
                .build();
        return FirebaseApp.initializeApp(options);
    }
}

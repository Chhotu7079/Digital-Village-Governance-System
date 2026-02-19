package com.dvgs.complaint.client;

import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification-service.url:http://localhost:8090/api/notifications}") String notificationUrl) {
        this.restTemplate = restTemplate;
        this.notificationUrl = notificationUrl;
    }

    public void sendNotification(String channel, String template, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "channel", channel,
                "template", template,
                "payload", payload,
                "sentAt", Instant.now().toString()
        );
        restTemplate.postForEntity(notificationUrl, new HttpEntity<>(body, headers), Void.class);
    }
}

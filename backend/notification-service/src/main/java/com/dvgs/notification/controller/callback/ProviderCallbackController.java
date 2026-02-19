package com.dvgs.notification.controller.callback;

import com.dvgs.notification.domain.NotificationStatus;
import com.dvgs.notification.repository.NotificationLogRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/callbacks")
public class ProviderCallbackController {

    private final NotificationLogRepository logRepository;

    public ProviderCallbackController(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @PostMapping("/twilio")
    public ResponseEntity<Void> handleTwilioCallback(@RequestBody Map<String, String> payload) {
        updateLog(payload.get("MessageSid"), payload.get("MessageStatus"));
        return ResponseEntity.ok().build();
    }

    private void updateLog(String providerMessageId, String status) {
        logRepository.findByProviderMessageId(providerMessageId).stream()
                .findFirst()
                .ifPresent(log -> {
                    log.setStatus(mapStatus(status));
                    log.setLastAttemptAt(OffsetDateTime.now());
                    logRepository.save(log);
                });
    }

    private NotificationStatus mapStatus(String status) {
        return switch (status == null ? "" : status.toUpperCase()) {
            case "DELIVERED" -> NotificationStatus.DELIVERED;
            case "FAILED", "UNDELIVERED" -> NotificationStatus.FAILED;
            default -> NotificationStatus.SENT;
        };
    }
}

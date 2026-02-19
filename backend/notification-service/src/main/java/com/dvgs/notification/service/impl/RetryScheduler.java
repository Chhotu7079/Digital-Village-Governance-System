package com.dvgs.notification.service.impl;

import com.dvgs.notification.domain.NotificationLog;
import com.dvgs.notification.domain.NotificationStatus;
import com.dvgs.notification.repository.NotificationLogRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(RetryScheduler.class);

    private final NotificationLogRepository logRepository;

    public RetryScheduler(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Scheduled(fixedDelayString = "${notification.retry.backoff-ms:5000}")
    public void scheduleRetries() {
        List<NotificationLog> failedLogs = logRepository.findAll().stream()
                .filter(logEntry -> logEntry.getStatus() == NotificationStatus.FAILED && logEntry.getAttemptCount() < 5)
                .toList();
        if (!failedLogs.isEmpty()) {
            log.info("Found {} failed notifications awaiting retry", failedLogs.size());
        }
        failedLogs.forEach(logEntry -> {
            logEntry.setStatus(NotificationStatus.RETRY_SCHEDULED);
            logEntry.setLastAttemptAt(OffsetDateTime.now());
            logRepository.save(logEntry);
            // TODO: push back to queue for reprocessing once queue infrastructure ready
        });
    }
}

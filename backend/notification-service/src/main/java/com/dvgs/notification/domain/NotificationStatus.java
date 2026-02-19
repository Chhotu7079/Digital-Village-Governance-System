package com.dvgs.notification.domain;

public enum NotificationStatus {
    QUEUED,
    PROCESSING,
    SENT,
    DELIVERED,
    FAILED,
    RETRY_SCHEDULED
}

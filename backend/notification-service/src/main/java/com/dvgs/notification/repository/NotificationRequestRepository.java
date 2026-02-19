package com.dvgs.notification.repository;

import com.dvgs.notification.domain.NotificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, String> {
}

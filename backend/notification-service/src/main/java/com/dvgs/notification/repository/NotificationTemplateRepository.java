package com.dvgs.notification.repository;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByCodeAndChannelAndLanguage(String code, ChannelType channel, String language);
    void deleteByCode(String code);
}

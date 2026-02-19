package com.dvgs.notification.service;

import com.dvgs.notification.domain.NotificationTemplate;
import com.dvgs.notification.dto.TemplateRequestDto;
import java.util.List;

public interface TemplateService {
    List<NotificationTemplate> findAll();
    NotificationTemplate upsert(TemplateRequestDto dto);
    void deleteByCode(String code);
}

package com.dvgs.notification.service.impl;

import com.dvgs.notification.domain.NotificationTemplate;
import com.dvgs.notification.dto.TemplateRequestDto;
import com.dvgs.notification.repository.NotificationTemplateRepository;
import com.dvgs.notification.service.TemplateService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;

    public TemplateServiceImpl(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public List<NotificationTemplate> findAll() {
        return templateRepository.findAll();
    }

    @Override
    public NotificationTemplate upsert(TemplateRequestDto dto) {
        NotificationTemplate template = templateRepository.findByCodeAndChannelAndLanguage(
                        dto.code(), dto.channel(), dto.language())
                .orElseGet(NotificationTemplate::new);
        template.setCode(dto.code());
        template.setChannel(dto.channel());
        template.setLanguage(dto.language());
        template.setTitle(dto.title());
        template.setBody(dto.body());
        template.setMetadata(dto.metadata());
        OffsetDateTime now = OffsetDateTime.now();
        if (template.getCreatedAt() == null) {
            template.setCreatedAt(now);
        }
        template.setUpdatedAt(now);
        return templateRepository.save(template);
    }

    @Override
    public void deleteByCode(String code) {
        templateRepository.deleteByCode(code);
    }
}

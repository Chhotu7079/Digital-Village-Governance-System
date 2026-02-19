package com.dvgs.scheme.service;

import com.dvgs.scheme.domain.AuditLog;
import com.dvgs.scheme.repository.AuditLogRepository;
import com.dvgs.scheme.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityType, String entityId, String action, String message, String metadataJson) {
        String actor = SecurityUtils.currentUserId();
        if (actor == null || actor.isBlank()) {
            actor = "SYSTEM";
        }

        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setActorUserId(actor);
        log.setMessage(message);
        log.setMetadata(metadataJson);

        auditLogRepository.save(log);
    }
}

package com.dvgs.scheme.controller;

import com.dvgs.scheme.domain.AuditLog;
import com.dvgs.scheme.dto.AuditDtos;
import com.dvgs.scheme.repository.AuditLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('OFFICIAL','ADMIN','SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/entity")
    public List<AuditDtos.AuditLogResponse> byEntity(
            @RequestParam String entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, PageRequest.of(0, safeLimit))
                .stream().map(this::toDto).toList();
    }

    @GetMapping("/actor/{actorUserId}")
    public List<AuditDtos.AuditLogResponse> byActor(
            @PathVariable String actorUserId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return auditLogRepository
                .findByActorUserIdOrderByCreatedAtDesc(actorUserId, PageRequest.of(0, safeLimit))
                .stream().map(this::toDto).toList();
    }

    private AuditDtos.AuditLogResponse toDto(AuditLog log) {
        return new AuditDtos.AuditLogResponse(
                log.getId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getActorUserId(),
                log.getMessage(),
                log.getMetadata(),
                log.getCreatedAt()
        );
    }
}

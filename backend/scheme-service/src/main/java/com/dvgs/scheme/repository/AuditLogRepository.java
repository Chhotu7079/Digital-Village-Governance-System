package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.AuditLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId, Pageable pageable);

    List<AuditLog> findByActorUserIdOrderByCreatedAtDesc(String actorUserId, Pageable pageable);
}

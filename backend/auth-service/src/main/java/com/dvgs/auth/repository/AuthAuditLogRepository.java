package com.dvgs.auth.repository;

import com.dvgs.auth.domain.AuthAuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
}

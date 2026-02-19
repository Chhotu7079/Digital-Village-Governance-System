package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.ComplaintAuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintAuditLogRepository extends JpaRepository<ComplaintAuditLog, UUID> {
    List<ComplaintAuditLog> findByComplaintIdOrderByCreatedAtDesc(UUID complaintId);
}

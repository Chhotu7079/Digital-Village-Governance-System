package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.ComplaintAuditLog;
import com.dvgs.complaint.dto.ComplaintAuditLogDto;
import com.dvgs.complaint.mapper.ComplaintMapper;
import com.dvgs.complaint.repository.ComplaintAuditLogRepository;
import com.dvgs.complaint.service.AuditService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final ComplaintAuditLogRepository auditLogRepository;
    private final ComplaintMapper complaintMapper;

    @Override
    @Transactional
    public void record(UUID complaintId, UUID actorId, String action, String details) {
        ComplaintAuditLog log = ComplaintAuditLog.builder()
                .complaintId(complaintId)
                .actorId(actorId)
                .action(action)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public List<ComplaintAuditLogDto> getLogs(UUID complaintId) {
        return auditLogRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId).stream()
                .map(complaintMapper::toAuditDto)
                .toList();
    }
}

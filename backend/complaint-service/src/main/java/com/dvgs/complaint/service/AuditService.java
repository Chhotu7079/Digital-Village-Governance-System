package com.dvgs.complaint.service;

import com.dvgs.complaint.dto.ComplaintAuditLogDto;
import java.util.List;
import java.util.UUID;

public interface AuditService {

    void record(UUID complaintId, UUID actorId, String action, String details);

    List<ComplaintAuditLogDto> getLogs(UUID complaintId);
}

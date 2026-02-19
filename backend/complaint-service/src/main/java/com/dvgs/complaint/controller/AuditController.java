package com.dvgs.complaint.controller;

import com.dvgs.complaint.dto.ComplaintAuditLogDto;
import com.dvgs.complaint.service.AuditService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints/{complaintId}/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICIAL','ADMIN')")
    public ResponseEntity<List<ComplaintAuditLogDto>> getAuditLogs(@PathVariable UUID complaintId) {
        return ResponseEntity.ok(auditService.getLogs(complaintId));
    }
}

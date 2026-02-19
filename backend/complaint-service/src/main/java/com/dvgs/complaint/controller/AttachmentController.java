package com.dvgs.complaint.controller;

import com.dvgs.complaint.dto.ComplaintAttachmentDto;
import com.dvgs.complaint.security.SecurityUtils;
import com.dvgs.complaint.service.AttachmentService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/complaints/{complaintId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CITIZEN','OFFICIAL','ADMIN')")
    public ResponseEntity<ComplaintAttachmentDto> upload(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @RequestParam("file") MultipartFile file) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(attachmentService.upload(complaintId, file, userId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComplaintAttachmentDto>> list(Authentication authentication, @PathVariable UUID complaintId) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        return ResponseEntity.ok(attachmentService.list(complaintId, userId, admin, officer));
    }

    @PostMapping("/{attachmentId}/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> issueToken(Authentication authentication,
            @PathVariable UUID complaintId,
            @PathVariable UUID attachmentId) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        return ResponseEntity.ok(attachmentService.issueDownloadToken(attachmentId, userId, admin, officer));
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @PathVariable UUID attachmentId,
            @RequestParam String token) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        byte[] payload = attachmentService.download(attachmentId, token, userId, admin, officer);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + attachmentId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(payload);
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('OFFICIAL','ADMIN')")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @PathVariable UUID attachmentId) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        attachmentService.delete(attachmentId, userId, admin, officer);
        return ResponseEntity.noContent().build();
    }
}

package com.dvgs.scheme.attachments;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping("/presign-upload")
    @PreAuthorize("hasRole('CITIZEN')")
    public AttachmentDtos.PresignUploadResponse presignUpload(@Valid @RequestBody AttachmentDtos.PresignUploadRequest req) {
        return attachmentService.presignUpload(req);
    }

    @PostMapping("/presign-download")
    @PreAuthorize("hasAnyRole('CITIZEN','OFFICIAL','ADMIN','SUPER_ADMIN')")
    public AttachmentDtos.PresignDownloadResponse presignDownload(@Valid @RequestBody AttachmentDtos.PresignDownloadRequest req) {
        return attachmentService.presignDownload(req);
    }
}

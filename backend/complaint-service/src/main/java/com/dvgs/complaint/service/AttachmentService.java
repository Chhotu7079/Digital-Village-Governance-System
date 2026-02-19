package com.dvgs.complaint.service;

import com.dvgs.complaint.dto.ComplaintAttachmentDto;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {

    ComplaintAttachmentDto upload(UUID complaintId, MultipartFile file, UUID uploadedBy);

    byte[] download(UUID attachmentId, String token, UUID requester, boolean admin, boolean officer);

    void delete(UUID attachmentId, UUID requestedBy, boolean admin, boolean officer);

    List<ComplaintAttachmentDto> list(UUID complaintId, UUID requester, boolean admin, boolean officer);

    String issueDownloadToken(UUID attachmentId, UUID requester, boolean admin, boolean officer);
}

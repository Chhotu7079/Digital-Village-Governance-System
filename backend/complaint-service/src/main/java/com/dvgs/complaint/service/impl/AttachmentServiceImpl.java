package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintAttachment;
import com.dvgs.complaint.dto.ComplaintAttachmentDto;
import com.dvgs.complaint.mapper.ComplaintMapper;
import com.dvgs.complaint.repository.ComplaintAttachmentRepository;
import com.dvgs.complaint.repository.ComplaintRepository;
import com.dvgs.complaint.security.SecurityUtils;
import com.dvgs.complaint.service.AccessControlService;
import com.dvgs.complaint.service.AttachmentService;
import com.dvgs.complaint.storage.AttachmentDownloadTokenService;
import com.dvgs.complaint.storage.AttachmentStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintAttachmentRepository attachmentRepository;
    private final AttachmentStorageService storageService;
    private final ComplaintMapper complaintMapper;
    private final AccessControlService accessControlService;
    private final AttachmentDownloadTokenService tokenService;

    @Override
    @Transactional
    public ComplaintAttachmentDto upload(UUID complaintId, MultipartFile file, UUID uploadedBy) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        AttachmentStorageService.StoredAttachment stored = storageService.store(file);
        ComplaintAttachment attachment = ComplaintAttachment.builder()
                .complaint(complaint)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .storagePath(stored.storagePath())
                .build();
        return complaintMapper.toAttachmentDto(attachmentRepository.save(attachment));
    }

    @Override
    public byte[] download(UUID attachmentId, String token, UUID requester, boolean admin, boolean officer) {
        ComplaintAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        accessControlService.assertCanAccess(attachment.getComplaint().getId(), requester, admin, officer);
        if (!tokenService.isValid(token, attachmentId)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        return storageService.load(attachment.getStoragePath());
    }

    @Override
    @Transactional
    public void delete(UUID attachmentId, UUID requestedBy, boolean admin, boolean officer) {
        ComplaintAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        accessControlService.assertCanAccess(attachment.getComplaint().getId(), requestedBy, admin, officer);
        storageService.delete(attachment.getStoragePath());
        attachmentRepository.delete(attachment);
    }

    @Override
    public List<ComplaintAttachmentDto> list(UUID complaintId, UUID requester, boolean admin, boolean officer) {
        accessControlService.assertCanAccess(complaintId, requester, admin, officer);
        return attachmentRepository.findByComplaintId(complaintId).stream()
                .map(complaintMapper::toAttachmentDto)
                .toList();
    }

    @Override
    public String issueDownloadToken(UUID attachmentId, UUID requester, boolean admin, boolean officer) {
        ComplaintAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        accessControlService.assertCanAccess(attachment.getComplaint().getId(), requester, admin, officer);
        return tokenService.issueToken(attachmentId);
    }

}

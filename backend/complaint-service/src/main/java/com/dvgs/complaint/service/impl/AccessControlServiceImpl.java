package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.repository.ComplaintRepository;
import com.dvgs.complaint.service.AccessControlService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessControlServiceImpl implements AccessControlService {

    private final ComplaintRepository complaintRepository;

    @Override
    public void assertCanAccess(UUID complaintId, UUID requesterId, boolean admin, boolean officer) {
        if (admin) {
            return;
        }
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        boolean isOwner = complaint.getCitizenId().equals(requesterId);
        boolean isAssignedOfficer = complaint.getAssignedOfficerId() != null
                && complaint.getAssignedOfficerId().equals(requesterId);
        if (!isOwner && !(officer && isAssignedOfficer)) {
            throw new AccessDeniedException(HttpStatus.FORBIDDEN.getReasonPhrase());
        }
    }
}

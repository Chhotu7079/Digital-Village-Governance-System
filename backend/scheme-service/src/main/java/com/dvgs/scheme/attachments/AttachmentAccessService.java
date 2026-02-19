package com.dvgs.scheme.attachments;

import com.dvgs.scheme.domain.SchemeApplication;
import com.dvgs.scheme.repository.SchemeApplicationRepository;
import com.dvgs.scheme.security.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentAccessService {

    private final SchemeApplicationRepository applicationRepository;

    /**
     * Validates that current authenticated user can access the provided fileRef.
     *
     * Rules:
     * - CITIZEN: must be applicant of the application that contains this fileRef
     * - OFFICIAL: must be assigned officer for that application
     * - ADMIN/SUPER_ADMIN: allowed
     */
    public void validateCanDownload(String fileRef) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("Missing authentication");
        }

        String userId = SecurityUtils.currentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Missing authenticated user");
        }
        boolean isAdmin = hasRole(auth, "ROLE_ADMIN") || hasRole(auth, "ROLE_SUPER_ADMIN");
        boolean isOfficial = hasRole(auth, "ROLE_OFFICIAL");
        boolean isCitizen = hasRole(auth, "ROLE_CITIZEN");

        if (isAdmin) {
            return;
        }

        // Find the application that contains this fileRef
        SchemeApplication app = applicationRepository.findById(findApplicationIdByFileRef(fileRef))
                .orElseThrow(() -> new EntityNotFoundException("Application not found for fileRef"));

        if (isCitizen) {
            if (!userId.equals(app.getApplicantUserId())) {
                throw new IllegalArgumentException("Access denied to requested file");
            }
            return;
        }

        if (isOfficial) {
            if (app.getAssignedOfficerId() == null || !userId.equals(app.getAssignedOfficerId())) {
                throw new IllegalArgumentException("Access denied to requested file");
            }
            return;
        }

        throw new IllegalArgumentException("Access denied to requested file");
    }

    private Long findApplicationIdByFileRef(String fileRef) {
        // We don't have a direct query yet; fall back to scanning docs via repository method added below.
        Long id = applicationRepository.findApplicationIdByDocumentFileRef(fileRef);
        if (id == null) {
            throw new EntityNotFoundException("Document not found for fileRef");
        }
        return id;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}

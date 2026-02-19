package com.dvgs.scheme.service;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.ApplicationListDtos;
import com.dvgs.scheme.repository.SchemeApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationQueryService {

    private final SchemeApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public Page<ApplicationListDtos.ApplicationListItem> listByStatus(ApplicationStatus status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<ApplicationListDtos.ApplicationListItem> pageResult = applicationRepository
                .findAnalyticsRowsByStatusAndSchemeStatus(status, SchemeStatus.ACTIVE, PageRequest.of(safePage, safeSize))
                .map(r -> new ApplicationListDtos.ApplicationListItem(
                        r.getId(),
                        r.getSchemeId(),
                        r.getApplicantUserId(),
                        r.getStatus(),
                        r.getAssignedOfficerId(),
                        r.getSubmittedAt(),
                        r.getUpdatedAt(),
                        r.getCurrentStatusSince()
                ));

        return pageResult;
    }
}

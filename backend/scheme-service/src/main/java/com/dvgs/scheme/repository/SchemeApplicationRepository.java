package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.ApplicationStatus;
import com.dvgs.scheme.domain.SchemeApplication;
import com.dvgs.scheme.domain.SchemeApplicationStatusHistory;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SchemeApplicationRepository extends JpaRepository<SchemeApplication, Long> {
    List<SchemeApplication> findByApplicantUserIdOrderByCreatedAtDesc(String applicantUserId);

    java.util.Optional<SchemeApplication> findFirstByApplicantUserIdAndScheme_IdOrderByCreatedAtDesc(String applicantUserId, Long schemeId);

    java.util.Optional<SchemeApplication> findFirstByApplicantUserIdAndScheme_IdAndStatusOrderByCreatedAtDesc(
            String applicantUserId,
            Long schemeId,
            ApplicationStatus status
    );

    java.util.Optional<SchemeApplication> findFirstByApplicantUserIdAndScheme_IdAndStatusInOrderByCreatedAtDesc(
            String applicantUserId,
            Long schemeId,
            java.util.Collection<ApplicationStatus> statuses
    );

    List<SchemeApplication> findByStatusOrderByUpdatedAtDesc(ApplicationStatus status);

    Page<SchemeApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    List<SchemeApplication> findByScheme_IdAndStatusOrderByUpdatedAtDesc(Long schemeId, ApplicationStatus status);

    @Query("select a.status as status, count(a) as cnt from SchemeApplication a group by a.status")
    List<Object[]> countByStatus();

    @Query("select a.scheme.id as schemeId, a.status as status, count(a) as cnt from SchemeApplication a group by a.scheme.id, a.status")
    List<Object[]> countBySchemeAndStatus();

    @Query("select a.scheme.id as schemeId, a.status as status, count(a) as cnt from SchemeApplication a join a.scheme s where s.status = :schemeStatus group by a.scheme.id, a.status")
    List<Object[]> countBySchemeAndStatusForSchemeStatus(@Param("schemeStatus") com.dvgs.scheme.domain.SchemeStatus schemeStatus);

    @Query("select count(a) from SchemeApplication a where a.status = :status and a.updatedAt < :olderThan")
    long countOlderThan(@Param("status") ApplicationStatus status, @Param("olderThan") Instant olderThan);

    /**
     * Counts applications currently in the given status whose last status-change timestamp is older than a threshold.
     * Uses status history table (max changedAt) for SLA aging.
     */
    @Query("""
            select count(a) from SchemeApplication a
            where a.status = :status
              and (
                select max(h.changedAt) from SchemeApplicationStatusHistory h
                where h.application = a
              ) < :olderThan
            """)
    long countOlderThanByHistory(@Param("status") ApplicationStatus status, @Param("olderThan") Instant olderThan);

    interface ApplicationAnalyticsRow {
        Long getId();

        Long getSchemeId();

        String getApplicantUserId();

        ApplicationStatus getStatus();

        String getAssignedOfficerId();

        Instant getSubmittedAt();

        Instant getUpdatedAt();

        Instant getCurrentStatusSince();
    }

    @Query("""
            select
              a.id as id,
              s.id as schemeId,
              a.applicantUserId as applicantUserId,
              a.status as status,
              a.assignedOfficerId as assignedOfficerId,
              a.submittedAt as submittedAt,
              a.updatedAt as updatedAt,
              max(h.changedAt) as currentStatusSince
            from SchemeApplication a
            join a.scheme s
            left join a.statusHistory h
            where a.status = :status
              and s.status = :schemeStatus
            group by a.id, s.id, a.applicantUserId, a.status, a.assignedOfficerId, a.submittedAt, a.updatedAt
            """)
    org.springframework.data.domain.Page<ApplicationAnalyticsRow> findAnalyticsRowsByStatusAndSchemeStatus(
            @Param("status") ApplicationStatus status,
            @Param("schemeStatus") com.dvgs.scheme.domain.SchemeStatus schemeStatus,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("select a.scheme.id as schemeId, count(a) as cnt from SchemeApplication a group by a.scheme.id order by cnt desc")
    List<Object[]> topSchemes(org.springframework.data.domain.Pageable pageable);

    @Query("select d.application.id from SchemeApplicationDocument d where d.fileRef = :fileRef")
    Long findApplicationIdByDocumentFileRef(@Param("fileRef") String fileRef);
}

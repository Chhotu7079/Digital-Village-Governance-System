package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID>, JpaSpecificationExecutor<Complaint> {

    List<Complaint> findByCitizenIdOrderByCreatedAtDesc(UUID citizenId);

    List<Complaint> findByAssignedOfficerIdOrderByCreatedAtDesc(UUID officerId);

    List<Complaint> findByDepartmentIdAndStatus(UUID departmentId, ComplaintStatus status);

    @Query("SELECT c FROM Complaint c WHERE c.status = :status AND c.expectedResolutionAt < current_timestamp")
    List<Complaint> findOverdueComplaints(@Param("status") ComplaintStatus status);

    Optional<Complaint> findTopByCitizenIdOrderByCreatedAtDesc(UUID citizenId);

    List<Complaint> findByStatusNotInAndExpectedResolutionAtBefore(List<ComplaintStatus> statuses, Instant timestamp);

    @Query("SELECT c FROM Complaint c WHERE c.expectedResolutionAt IS NOT NULL AND c.expectedResolutionAt < :cutoff AND c.status NOT IN :terminals")
    List<Complaint> findOverdueComplaints(@Param("terminals") List<ComplaintStatus> terminals, @Param("cutoff") Instant cutoff);

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countByStatus();

    @Query("SELECT c.departmentId, d.name, SUM(CASE WHEN c.status IN ('SUBMITTED','IN_REVIEW','IN_PROGRESS') THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.status = 'ESCALATED' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN c.status IN ('RESOLVED','CLOSED') THEN 1 ELSE 0 END) " +
            "FROM Complaint c JOIN Department d ON c.departmentId = d.id GROUP BY c.departmentId, d.name")
    List<Object[]> departmentStats();

    @Query("SELECT COUNT(c), SUM(CASE WHEN c.closedAt IS NOT NULL AND c.closedAt <= c.expectedResolutionAt THEN 1 ELSE 0 END) " +
            "FROM Complaint c WHERE c.expectedResolutionAt IS NOT NULL")
    Object[] slaStats();
}

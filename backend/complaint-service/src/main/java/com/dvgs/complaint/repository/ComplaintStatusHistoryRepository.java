package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.ComplaintStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintStatusHistoryRepository extends JpaRepository<ComplaintStatusHistory, UUID> {

    List<ComplaintStatusHistory> findByComplaintIdOrderByStatusChangedAtAsc(UUID complaintId);
}

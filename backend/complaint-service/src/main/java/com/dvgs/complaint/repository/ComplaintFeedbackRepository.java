package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.ComplaintFeedback;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintFeedbackRepository extends JpaRepository<ComplaintFeedback, UUID> {

    List<ComplaintFeedback> findByComplaintId(UUID complaintId);
}

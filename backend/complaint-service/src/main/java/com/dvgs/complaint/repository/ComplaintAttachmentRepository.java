package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.ComplaintAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, UUID> {

    List<ComplaintAttachment> findByComplaintId(UUID complaintId);
}

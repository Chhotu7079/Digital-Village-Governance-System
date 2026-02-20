package com.dvgs.complaint.service;

import com.dvgs.complaint.domain.ComplaintPriority;
import com.dvgs.complaint.domain.ComplaintStatus;
import com.dvgs.complaint.dto.ComplaintAssignmentRequest;
import com.dvgs.complaint.dto.ComplaintCreateRequest;
import com.dvgs.complaint.dto.ComplaintDetail;
import com.dvgs.complaint.dto.ComplaintFeedbackDto;
import com.dvgs.complaint.dto.ComplaintFeedbackRequest;
import com.dvgs.complaint.dto.ComplaintStatusHistoryDto;
import com.dvgs.complaint.dto.ComplaintStatusUpdateRequest;
import com.dvgs.complaint.dto.ComplaintSummary;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintService {

    ComplaintDetail createComplaint(ComplaintCreateRequest request);

    ComplaintDetail assignComplaint(UUID complaintId, ComplaintAssignmentRequest request);

    ComplaintDetail updateStatus(UUID complaintId, ComplaintStatusUpdateRequest request);

    ComplaintFeedbackDto addFeedback(ComplaintFeedbackRequest request);

    ComplaintDetail getComplaint(UUID complaintId);

    List<ComplaintSummary> listComplaintsForCitizen(UUID citizenId);

    List<ComplaintSummary> listAssignedComplaints(UUID officerId);

    Page<ComplaintSummary> searchComplaints(ComplaintStatus status, ComplaintPriority priority,
                                            UUID citizenId, UUID officerId,
                                            UUID departmentId, Instant from, Instant to,
                                            Pageable pageable);

    List<ComplaintStatusHistoryDto> getHistory(UUID complaintId);
}

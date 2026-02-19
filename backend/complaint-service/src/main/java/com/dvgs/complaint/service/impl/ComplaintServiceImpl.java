package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintFeedback;
import com.dvgs.complaint.domain.ComplaintPriority;
import com.dvgs.complaint.domain.ComplaintStatus;
import com.dvgs.complaint.domain.ComplaintStatusHistory;
import com.dvgs.complaint.event.ComplaintEvent;
import com.dvgs.complaint.event.ComplaintEventPublisher;
import com.dvgs.complaint.dto.ComplaintAssignmentRequest;
import com.dvgs.complaint.dto.ComplaintCreateRequest;
import com.dvgs.complaint.dto.ComplaintDetail;
import com.dvgs.complaint.dto.ComplaintFeedbackDto;
import com.dvgs.complaint.dto.ComplaintFeedbackRequest;
import com.dvgs.complaint.dto.ComplaintStatusHistoryDto;
import com.dvgs.complaint.dto.ComplaintStatusUpdateRequest;
import com.dvgs.complaint.dto.ComplaintSummary;
import com.dvgs.complaint.mapper.ComplaintMapper;
import com.dvgs.complaint.metrics.ComplaintMetrics;
import com.dvgs.complaint.service.AuditService;
import com.dvgs.complaint.repository.ComplaintFeedbackRepository;
import com.dvgs.complaint.repository.ComplaintRepository;
import com.dvgs.complaint.repository.ComplaintStatusHistoryRepository;
import com.dvgs.complaint.repository.DepartmentRepository;
import com.dvgs.complaint.service.query.ComplaintSpecifications;
import com.dvgs.complaint.service.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final ComplaintFeedbackRepository feedbackRepository;
    private final DepartmentRepository departmentRepository;
    private final ComplaintMapper complaintMapper;
    private final ComplaintEventPublisher eventPublisher;
    private final AuditService auditService;
    private final ComplaintMetrics complaintMetrics;

    @Override
    @Transactional
    public ComplaintDetail createComplaint(ComplaintCreateRequest request) {
        if (request.getCitizenId() == null) {
            throw new IllegalArgumentException("Citizen context missing for complaint creation");
        }
        Complaint complaint = complaintMapper.toEntity(request);
        departmentRepository.findById(request.getDepartmentId())
                .ifPresent(dept -> {
                    if (dept.getSlaHours() != null) {
                        complaint.setExpectedResolutionAt(Instant.now().plusSeconds(dept.getSlaHours() * 3600L));
                    }
                });
        Complaint saved = complaintRepository.save(complaint);
        historyRepository.save(ComplaintStatusHistory.builder()
                .complaint(saved)
                .fromStatus(null)
                .toStatus(saved.getStatus())
                .changedBy(request.getCitizenId())
                .statusChangedAt(Instant.now())
                .build());
        auditService.record(saved.getId(), request.getCitizenId(), "COMPLAINT_CREATED", "Complaint submitted");
        publishEvent(saved, "COMPLAINT_CREATED", "Complaint submitted");
        complaintMetrics.incrementCreated();
        return getComplaint(saved.getId());
    }

    @Override
    @Transactional
    public ComplaintDetail assignComplaint(UUID complaintId, ComplaintAssignmentRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        if (request.getAssignedBy() == null) {
            throw new IllegalArgumentException("Assignment actor required");
        }
        ComplaintStatus previous = complaint.getStatus();
        complaint.setAssignedOfficerId(request.getOfficerId());
        complaint.setStatus(ComplaintStatus.IN_REVIEW);
        Complaint updated = complaintRepository.save(complaint);
        historyRepository.save(ComplaintStatusHistory.builder()
                .complaint(updated)
                .fromStatus(previous)
                .toStatus(updated.getStatus())
                .changedBy(request.getAssignedBy())
                .statusChangedAt(Instant.now())
                .build());
        auditService.record(updated.getId(), request.getAssignedBy(), "COMPLAINT_ASSIGNED", "Assigned to " + request.getOfficerId());
        publishEvent(updated, "COMPLAINT_ASSIGNED", "Complaint assigned to officer");
        return getComplaint(updated.getId());
    }

    @Override
    @Transactional
    public ComplaintDetail updateStatus(UUID complaintId, ComplaintStatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        if (request.getChangedBy() == null) {
            throw new IllegalArgumentException("Status change actor required");
        }
        ComplaintStatus previous = complaint.getStatus();
        complaint.setStatus(request.getToStatus());
        if (request.getToStatus() == ComplaintStatus.RESOLVED || request.getToStatus() == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(Instant.now());
            complaintMetrics.incrementResolved();
        }
        Complaint updated = complaintRepository.save(complaint);
        historyRepository.save(ComplaintStatusHistory.builder()
                .complaint(updated)
                .fromStatus(previous)
                .toStatus(updated.getStatus())
                .changedBy(request.getChangedBy())
                .remarks(request.getRemarks())
                .statusChangedAt(Instant.now())
                .build());
        auditService.record(updated.getId(), request.getChangedBy(), "COMPLAINT_STATUS_CHANGED", request.getRemarks());
        publishEvent(updated, "COMPLAINT_STATUS_CHANGED", "Complaint status updated to " + updated.getStatus());
        return getComplaint(updated.getId());
    }

    public void publishEscalation(Complaint complaint) {
        auditService.record(complaint.getId(), null, "COMPLAINT_ESCALATED", "SLA breach auto-escalation");
        publishEvent(complaint, "COMPLAINT_ESCALATED", "Complaint auto-escalated due to SLA breach");
        complaintMetrics.incrementEscalated();
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto addFeedback(ComplaintFeedbackRequest request) {
        if (request.getCitizenId() == null) {
            throw new IllegalArgumentException("Citizen context missing for feedback");
        }
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        ComplaintFeedback saved = feedbackRepository.save(ComplaintFeedback.builder()
                .complaint(complaint)
                .citizenId(request.getCitizenId())
                .rating(request.getRating())
                .comments(request.getComments())
                .build());
        auditService.record(complaint.getId(), request.getCitizenId(), "COMPLAINT_FEEDBACK", request.getComments());
        publishEvent(complaint, "COMPLAINT_FEEDBACK", "Feedback submitted");
        return complaintMapper.toFeedbackDto(saved);
    }

    @Override
    public ComplaintDetail getComplaint(UUID complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found"));
        return complaintMapper.toDetail(complaint);
    }

    @Override
    public List<ComplaintSummary> listComplaintsForCitizen(UUID citizenId) {
        return complaintMapper.toSummaries(complaintRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId));
    }

    @Override
    public List<ComplaintSummary> listAssignedComplaints(UUID officerId) {
        return complaintMapper.toSummaries(complaintRepository.findByAssignedOfficerIdOrderByCreatedAtDesc(officerId));
    }

    @Override
    public Page<ComplaintSummary> searchComplaints(ComplaintStatus status, ComplaintPriority priority,
                                                   UUID citizenId, UUID officerId,
                                                   UUID departmentId, Instant from, Instant to,
                                                   Pageable pageable) {
        Specification<Complaint> spec = Specification.where(ComplaintSpecifications.status(status))
                .and(ComplaintSpecifications.priority(priority))
                .and(ComplaintSpecifications.citizen(citizenId))
                .and(ComplaintSpecifications.assignedOfficer(officerId))
                .and(ComplaintSpecifications.department(departmentId))
                .and(ComplaintSpecifications.dateRange(from, to));
        return complaintRepository.findAll(spec, pageable).map(complaintMapper::toSummary);
    }

    @Override
    public List<ComplaintStatusHistoryDto> getHistory(UUID complaintId) {
        List<ComplaintStatusHistory> history = historyRepository.findByComplaintIdOrderByStatusChangedAtAsc(complaintId);
        return history.stream().map(complaintMapper::toHistoryDto).toList();
    }

    void publishEvent(Complaint complaint, String type, String description) {
        ComplaintEvent event = ComplaintEvent.builder()
                .complaintId(complaint.getId())
                .citizenId(complaint.getCitizenId())
                .departmentId(complaint.getDepartmentId())
                .assignedOfficerId(complaint.getAssignedOfficerId())
                .status(complaint.getStatus())
                .type(type)
                .timestamp(Instant.now())
                .description(description)
                .build();
        eventPublisher.publish(event);
    }
}

package com.dvgs.complaint.controller;

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
import com.dvgs.complaint.security.SecurityUtils;
import com.dvgs.complaint.service.AccessControlService;
import com.dvgs.complaint.service.ComplaintService;
import com.dvgs.complaint.service.FeedbackService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;
    private final FeedbackService feedbackService;
    private final AccessControlService accessControlService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CITIZEN','OFFICIAL','ADMIN')")
    public ResponseEntity<ComplaintDetail> createComplaint(
            Authentication authentication,
            @Valid @RequestBody ComplaintCreateRequest request) {
        request.setCitizenId(UUID.fromString(authentication.getName()));
        return ResponseEntity.ok(complaintService.createComplaint(request));
    }

    @PutMapping("/{complaintId}/assign")
    @PreAuthorize("hasAnyRole('OFFICIAL','ADMIN')")
    public ResponseEntity<ComplaintDetail> assignComplaint(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @Valid @RequestBody ComplaintAssignmentRequest request) {
        request.setAssignedBy(UUID.fromString(authentication.getName()));
        return ResponseEntity.ok(complaintService.assignComplaint(complaintId, request));
    }

    @PutMapping("/{complaintId}/status")
    @PreAuthorize("hasAnyRole('OFFICIAL','ADMIN')")
    public ResponseEntity<ComplaintDetail> updateStatus(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @Valid @RequestBody ComplaintStatusUpdateRequest request) {
        request.setChangedBy(UUID.fromString(authentication.getName()));
        return ResponseEntity.ok(complaintService.updateStatus(complaintId, request));
    }

    @GetMapping("/{complaintId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComplaintDetail> getComplaint(Authentication authentication, @PathVariable UUID complaintId) {
        UUID requester = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        accessControlService.assertCanAccess(complaintId, requester, admin, officer);
        return ResponseEntity.ok(complaintService.getComplaint(complaintId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ComplaintSummary>> listComplaints(
            Authentication authentication,
            @RequestParam(required = false) UUID citizenId,
            @RequestParam(required = false) UUID officerId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String createdFrom,
            @RequestParam(required = false) String createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID requester = UUID.fromString(authentication.getName());
        ComplaintStatus statusEnum = parseStatus(status);
        ComplaintPriority priorityEnum = parsePriority(priority);
        UUID effectiveCitizen = citizenId;
        if (effectiveCitizen == null && officerId == null) {
            effectiveCitizen = requester;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ComplaintSummary> result = complaintService.searchComplaints(
                statusEnum,
                priorityEnum,
                effectiveCitizen,
                officerId,
                departmentId,
                parseInstant(createdFrom),
                parseInstant(createdTo),
                pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{complaintId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComplaintStatusHistoryDto>> getHistory(Authentication authentication, @PathVariable UUID complaintId) {
        UUID requester = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        accessControlService.assertCanAccess(complaintId, requester, admin, officer);
        return ResponseEntity.ok(complaintService.getHistory(complaintId));
    }

    @PostMapping("/{complaintId}/feedback")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ComplaintFeedbackDto> addFeedback(
            Authentication authentication,
            @PathVariable UUID complaintId,
            @Valid @RequestBody ComplaintFeedbackRequest request) {
        request.setComplaintId(complaintId);
        request.setCitizenId(UUID.fromString(authentication.getName()));
        return ResponseEntity.ok(complaintService.addFeedback(request));
    }

    @GetMapping("/{complaintId}/feedback")
    public ResponseEntity<List<ComplaintFeedbackDto>> getFeedback(Authentication authentication, @PathVariable UUID complaintId) {
        UUID requester = UUID.fromString(authentication.getName());
        boolean admin = SecurityUtils.hasRole(authentication, "ADMIN");
        boolean officer = SecurityUtils.hasRole(authentication, "OFFICIAL");
        accessControlService.assertCanAccess(complaintId, requester, admin, officer);
        return ResponseEntity.ok(feedbackService.getFeedbackForComplaint(complaintId));
    }

    private ComplaintStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ComplaintStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }
    }

    private ComplaintPriority parsePriority(String priority) {
        if (priority == null || priority.isBlank()) {
            return null;
        }
        try {
            return ComplaintPriority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid priority value");
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use ISO-8601");
        }
    }
}

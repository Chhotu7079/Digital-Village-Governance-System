package com.dvgs.complaint.mapper;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintAttachment;
import com.dvgs.complaint.domain.ComplaintAuditLog;
import com.dvgs.complaint.domain.ComplaintFeedback;
import com.dvgs.complaint.domain.ComplaintStatusHistory;
import com.dvgs.complaint.dto.ComplaintAttachmentDto;
import com.dvgs.complaint.dto.ComplaintAuditLogDto;
import com.dvgs.complaint.dto.ComplaintCreateRequest;
import com.dvgs.complaint.dto.ComplaintDetail;
import com.dvgs.complaint.dto.ComplaintFeedbackDto;
import com.dvgs.complaint.dto.ComplaintSummary;
import com.dvgs.complaint.dto.ComplaintStatusHistoryDto;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedOfficerId", ignore = true)
    @Mapping(target = "departmentId", source = "departmentId")
    @Mapping(target = "status", expression = "java(com.dvgs.complaint.domain.ComplaintStatus.SUBMITTED)")
    @Mapping(target = "expectedResolutionAt", ignore = true)
    @Mapping(target = "closedAt", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "feedbackEntries", ignore = true)
    Complaint toEntity(ComplaintCreateRequest request);

    ComplaintSummary toSummary(Complaint complaint);

    @Mapping(target = "attachments", source = "attachments")
    @Mapping(target = "history", source = "statusHistory")
    ComplaintDetail toDetail(Complaint complaint);

    ComplaintAttachmentDto toAttachmentDto(ComplaintAttachment attachment);

    ComplaintStatusHistoryDto toHistoryDto(ComplaintStatusHistory history);

    ComplaintFeedbackDto toFeedbackDto(ComplaintFeedback feedback);

    ComplaintAuditLogDto toAuditDto(ComplaintAuditLog auditLog);

    List<ComplaintSummary> toSummaries(List<Complaint> complaints);
}

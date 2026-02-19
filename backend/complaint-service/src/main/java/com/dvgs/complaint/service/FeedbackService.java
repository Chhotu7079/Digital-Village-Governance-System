package com.dvgs.complaint.service;

import com.dvgs.complaint.dto.ComplaintFeedbackDto;
import java.util.List;
import java.util.UUID;

public interface FeedbackService {

    List<ComplaintFeedbackDto> getFeedbackForComplaint(UUID complaintId);
}

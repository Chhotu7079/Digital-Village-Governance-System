package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.dto.ComplaintFeedbackDto;
import com.dvgs.complaint.mapper.ComplaintMapper;
import com.dvgs.complaint.repository.ComplaintFeedbackRepository;
import com.dvgs.complaint.service.FeedbackService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final ComplaintFeedbackRepository feedbackRepository;
    private final ComplaintMapper complaintMapper;

    @Override
    public List<ComplaintFeedbackDto> getFeedbackForComplaint(UUID complaintId) {
        return feedbackRepository.findByComplaintId(complaintId).stream()
                .map(complaintMapper::toFeedbackDto)
                .toList();
    }
}

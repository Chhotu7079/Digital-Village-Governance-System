package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.config.sla.SlaProperties;
import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintStatus;
import com.dvgs.complaint.repository.ComplaintRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaMonitorService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintServiceImpl complaintServiceImpl;
    private final SlaProperties slaProperties;

    @Scheduled(fixedDelayString = "${complaint.sla.check-interval:PT15M}")
    @Transactional
    public void monitorOverdueComplaints() {
        Instant now = Instant.now();
        List<Complaint> overdue = complaintRepository.findOverdueComplaints(
                List.copyOf(EnumSet.of(ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED, ComplaintStatus.REJECTED, ComplaintStatus.ESCALATED)),
                now.minus(slaProperties.getEscalationDelay()));
        overdue.forEach(this::escalateComplaint);
    }

    private void escalateComplaint(Complaint complaint) {
        if (complaint.getStatus() == ComplaintStatus.ESCALATED) {
            return;
        }
        log.warn("Escalating complaint {} due to SLA breach", complaint.getId());
        complaint.setStatus(ComplaintStatus.ESCALATED);
        complaintRepository.save(complaint);
        complaintServiceImpl.publishEscalation(complaint);
    }
}

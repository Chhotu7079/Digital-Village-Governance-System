package com.dvgs.complaint.event;

public interface ComplaintEventPublisher {
    void publish(ComplaintEvent event);
}

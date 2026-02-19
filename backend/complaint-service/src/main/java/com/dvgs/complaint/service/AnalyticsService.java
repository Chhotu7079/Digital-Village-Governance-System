package com.dvgs.complaint.service;

import com.dvgs.complaint.dto.ComplaintStatusCount;
import com.dvgs.complaint.dto.DepartmentAnalytics;
import com.dvgs.complaint.dto.SlaAnalytics;
import java.util.List;

public interface AnalyticsService {

    List<ComplaintStatusCount> statusCounts();

    List<DepartmentAnalytics> departmentAnalytics();

    SlaAnalytics slaAnalytics();
}

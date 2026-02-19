package com.dvgs.complaint.service.query;

import com.dvgs.complaint.domain.Complaint;
import com.dvgs.complaint.domain.ComplaintPriority;
import com.dvgs.complaint.domain.ComplaintStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ComplaintSpecifications {

    private ComplaintSpecifications() {
    }

    public static Specification<Complaint> citizen(UUID citizenId) {
        return (root, query, cb) -> citizenId == null ? null : cb.equal(root.get("citizenId"), citizenId);
    }

    public static Specification<Complaint> assignedOfficer(UUID officerId) {
        return (root, query, cb) -> officerId == null ? null : cb.equal(root.get("assignedOfficerId"), officerId);
    }

    public static Specification<Complaint> department(UUID departmentId) {
        return (root, query, cb) -> departmentId == null ? null : cb.equal(root.get("departmentId"), departmentId);
    }

    public static Specification<Complaint> status(ComplaintStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Complaint> priority(ComplaintPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Complaint> dateRange(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return cb.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}

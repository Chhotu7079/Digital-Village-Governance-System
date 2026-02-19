package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.Scheme;
import com.dvgs.scheme.domain.SchemeStatus;
import org.springframework.data.jpa.domain.Specification;

public final class SchemeSpecifications {

    private SchemeSpecifications() {
    }

    public static Specification<Scheme> statusEquals(SchemeStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Scheme> departmentEquals(String department) {
        return (root, query, cb) -> {
            if (department == null || department.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("department")), department.trim().toLowerCase());
        };
    }

    public static Specification<Scheme> queryLike(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("schemeCode")), like)
            );
        };
    }

    public static Specification<Scheme> excludeArchivedUnless(boolean includeArchived) {
        return (root, query, cb) -> includeArchived
                ? cb.conjunction()
                : cb.notEqual(root.get("status"), SchemeStatus.ARCHIVED);
    }
}

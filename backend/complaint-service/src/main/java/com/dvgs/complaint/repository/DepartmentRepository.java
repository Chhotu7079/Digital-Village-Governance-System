package com.dvgs.complaint.repository;

import com.dvgs.complaint.domain.Department;
import com.dvgs.complaint.domain.DepartmentCategory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByCategory(DepartmentCategory category);

    Optional<Department> findByNameIgnoreCase(String name);
}

package com.dvgs.complaint.service;

import com.dvgs.complaint.domain.DepartmentCategory;
import com.dvgs.complaint.dto.DepartmentDto;
import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentDto create(DepartmentDto dto);

    List<DepartmentDto> listAll();

    List<DepartmentDto> listByCategory(DepartmentCategory category);

    DepartmentDto get(UUID id);
}

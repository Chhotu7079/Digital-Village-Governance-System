package com.dvgs.complaint.service.impl;

import com.dvgs.complaint.domain.Department;
import com.dvgs.complaint.domain.DepartmentCategory;
import com.dvgs.complaint.dto.DepartmentDto;
import com.dvgs.complaint.mapper.DepartmentMapper;
import com.dvgs.complaint.repository.DepartmentRepository;
import com.dvgs.complaint.service.DepartmentService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentDto create(DepartmentDto dto) {
        Department department = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .slaHours(dto.getSlaHours())
                .leadOfficerId(dto.getLeadOfficerId())
                .build();
        return departmentMapper.toDto(departmentRepository.save(department));
    }

    @Override
    public List<DepartmentDto> listAll() {
        return departmentRepository.findAll().stream().map(departmentMapper::toDto).toList();
    }

    @Override
    public List<DepartmentDto> listByCategory(DepartmentCategory category) {
        return departmentRepository.findByCategory(category).stream().map(departmentMapper::toDto).toList();
    }

    @Override
    public DepartmentDto get(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        return departmentMapper.toDto(department);
    }
}

package com.dvgs.complaint.mapper;

import com.dvgs.complaint.domain.Department;
import com.dvgs.complaint.dto.DepartmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    DepartmentDto toDto(Department department);
}

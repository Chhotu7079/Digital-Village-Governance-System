package com.dvgs.complaint.controller;

import com.dvgs.complaint.domain.DepartmentCategory;
import com.dvgs.complaint.dto.DepartmentDto;
import com.dvgs.complaint.service.DepartmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto request) {
        return ResponseEntity.ok(departmentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DepartmentDto>> listDepartments(
            @RequestParam(required = false) DepartmentCategory category) {
        if (category != null) {
            return ResponseEntity.ok(departmentService.listByCategory(category));
        }
        return ResponseEntity.ok(departmentService.listAll());
    }

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentDto> getDepartment(@PathVariable UUID departmentId) {
        return ResponseEntity.ok(departmentService.get(departmentId));
    }
}

package com.dvgs.notification.controller;

import com.dvgs.notification.domain.NotificationTemplate;
import com.dvgs.notification.dto.TemplateRequestDto;
import com.dvgs.notification.service.TemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<List<NotificationTemplate>> getTemplates() {
        return ResponseEntity.ok(templateService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<NotificationTemplate> upsertTemplate(@Valid @RequestBody TemplateRequestDto request) {
        return ResponseEntity.ok(templateService.upsert(request));
    }

    @DeleteMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String code) {
        templateService.deleteByCode(code);
        return ResponseEntity.noContent().build();
    }
}

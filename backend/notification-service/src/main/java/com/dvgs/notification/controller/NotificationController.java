package com.dvgs.notification.controller;

import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.dto.NotificationStatusDto;
import com.dvgs.notification.service.NotificationDispatchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationDispatchService dispatchService;

    public NotificationController(NotificationDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<String> submitNotification(@Valid @RequestBody NotificationRequestDto request) {
        return ResponseEntity.accepted().body(dispatchService.enqueueNotification(request));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICIAL','SUPER_ADMIN')")
    public ResponseEntity<List<NotificationStatusDto>> getStatus(@PathVariable String requestId) {
        return ResponseEntity.ok(dispatchService.getStatus(requestId));
    }
}

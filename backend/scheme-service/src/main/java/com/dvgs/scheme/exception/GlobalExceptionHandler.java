package com.dvgs.scheme.exception;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MissingRequiredDocumentsException.class)
    public ResponseEntity<?> handleMissingDocs(MissingRequiredDocumentsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "MISSING_REQUIRED_DOCUMENTS",
                "message", ex.getMessage(),
                "missingDocTypes", ex.getMissingDocTypes()
        ));
    }

    @ExceptionHandler(MissingUploadedDocumentsException.class)
    public ResponseEntity<?> handleMissingUploadedDocs(MissingUploadedDocumentsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "MISSING_UPLOADED_DOCUMENTS",
                "message", ex.getMessage(),
                "applicationId", ex.getApplicationId(),
                "missingDocTypes", ex.getMissingDocTypes()
        ));
    }

    @ExceptionHandler(SchemeArchivedException.class)
    public ResponseEntity<?> handleSchemeArchived(SchemeArchivedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "SCHEME_ARCHIVED",
                "message", ex.getMessage(),
                "schemeId", ex.getSchemeId()
        ));
    }

    @ExceptionHandler(ReapplyCooldownException.class)
    public ResponseEntity<?> handleCooldown(ReapplyCooldownException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "REAPPLY_COOLDOWN",
                "message", ex.getMessage(),
                "remainingDays", ex.getRemainingDays()
        ));
    }

    @ExceptionHandler(ActiveApplicationExistsException.class)
    public ResponseEntity<?> handleActiveExists(ActiveApplicationExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "ACTIVE_APPLICATION_EXISTS",
                "message", ex.getMessage(),
                "existingApplicationId", ex.getExistingApplicationId(),
                "existingStatus", ex.getExistingStatus().name()
        ));
    }

    @ExceptionHandler({IllegalArgumentException.class, DataIntegrityViolationException.class})
    public ResponseEntity<?> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        java.util.Map<String, String> fieldErrors = new java.util.LinkedHashMap<>();
        for (var err : ex.getBindingResult().getFieldErrors()) {
            // If multiple validations fail on same field, keep the first message (usually most relevant)
            fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now().toString(),
                "code", "VALIDATION_ERROR",
                "message", "Request validation failed",
                "fieldErrors", fieldErrors
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", "Unexpected error"));
    }

    private Map<String, Object> error(String code, String message) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "code", code,
                "message", message
        );
    }
}

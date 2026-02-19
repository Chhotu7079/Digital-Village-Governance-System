package com.dvgs.scheme.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 128)
    private String entityId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(name = "actor_user_id", nullable = false, length = 128)
    private String actorUserId;

    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * JSON metadata (stored as JSONB in Postgres).
     * Kept as raw JSON string here to avoid extra dependencies.
     */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

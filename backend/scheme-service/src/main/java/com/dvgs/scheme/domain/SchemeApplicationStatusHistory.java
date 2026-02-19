package com.dvgs.scheme.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scheme_application_status_history")
public class SchemeApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private SchemeApplication application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private ApplicationStatus toStatus;

    @Column(name = "changed_by", nullable = false, length = 128)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt = Instant.now();

    @Column(columnDefinition = "TEXT")
    private String remarks;
}

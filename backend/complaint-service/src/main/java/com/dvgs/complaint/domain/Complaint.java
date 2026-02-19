package com.dvgs.complaint.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @Column(name = "citizen_id", nullable = false)
    private UUID citizenId;

    @Column(name = "assigned_officer_id")
    private UUID assignedOfficerId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private ComplaintPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private ComplaintChannel channel;

    @Column(name = "expected_resolution_at")
    private Instant expectedResolutionAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @ElementCollection
    @Column(name = "tags")
    private Set<String> tags = new HashSet<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ComplaintStatusHistory> statusHistory = new HashSet<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ComplaintAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ComplaintFeedback> feedbackEntries = new HashSet<>();
}

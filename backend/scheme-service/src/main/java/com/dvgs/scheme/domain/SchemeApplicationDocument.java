package com.dvgs.scheme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "scheme_application_documents",
        uniqueConstraints = @UniqueConstraint(name = "uk_application_doc_type", columnNames = {"application_id", "doc_type"})
)
public class SchemeApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private SchemeApplication application;

    @Column(name = "doc_type", nullable = false, length = 64)
    private String docType;

    @Column(name = "file_ref", columnDefinition = "TEXT")
    private String fileRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "verified_status", nullable = false, length = 32)
    private DocumentVerifiedStatus verifiedStatus = DocumentVerifiedStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}

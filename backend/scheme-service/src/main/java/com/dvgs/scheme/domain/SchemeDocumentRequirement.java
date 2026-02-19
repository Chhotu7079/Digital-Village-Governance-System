package com.dvgs.scheme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "scheme_document_requirements",
        uniqueConstraints = @UniqueConstraint(name = "uk_scheme_doc_type", columnNames = {"scheme_id", "doc_type"})
)
public class SchemeDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;

    @Column(name = "doc_type", nullable = false, length = 64)
    private String docType;

    @Column(nullable = false)
    private boolean required = true;
}

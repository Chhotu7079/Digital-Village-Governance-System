package com.dvgs.scheme.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "scheme_eligibility_rules")
public class SchemeEligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scheme_id", nullable = false, unique = true)
    private Scheme scheme;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "min_income")
    private Long minIncome;

    @Column(name = "max_income")
    private Long maxIncome;

    /**
     * If set, must match profile gender (case-insensitive). Example: MALE/FEMALE/OTHER
     */
    @Column(name = "gender", length = 16)
    private String gender;

    /**
     * If set, must match profile category (case-insensitive). Example: SC/ST/OBC/GEN
     */
    @Column(name = "category", length = 16)
    private String category;
}

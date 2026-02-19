package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.SchemeEligibilityRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchemeEligibilityRuleRepository extends JpaRepository<SchemeEligibilityRule, Long> {
    Optional<SchemeEligibilityRule> findByScheme_Id(Long schemeId);
}

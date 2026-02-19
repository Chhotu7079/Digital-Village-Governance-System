package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.Scheme;
import com.dvgs.scheme.domain.SchemeStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SchemeRepository extends JpaRepository<Scheme, Long>, JpaSpecificationExecutor<Scheme> {
    Optional<Scheme> findBySchemeCode(String schemeCode);

    boolean existsBySchemeCode(String schemeCode);

    List<Scheme> findByStatus(SchemeStatus status);
}

package com.dvgs.scheme.repository;

import com.dvgs.scheme.domain.SchemeDocumentRequirement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SchemeDocumentRequirementRepository extends JpaRepository<SchemeDocumentRequirement, Long> {

    @Query("select r.docType from SchemeDocumentRequirement r where r.scheme.id = :schemeId and r.required = true")
    List<String> findRequiredDocTypes(@Param("schemeId") Long schemeId);
}

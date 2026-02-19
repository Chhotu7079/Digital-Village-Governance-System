package com.dvgs.scheme.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

public class EligibilityDtos {

    public record UpsertEligibilityRuleRequest(
            @Min(0) Integer minAge,
            @Min(0) Integer maxAge,
            @Min(0) Long minIncome,
            @Min(0) Long maxIncome,
            @Size(max = 16) String gender,
            @Size(max = 16) String category
    ) {}

    public record EligibilityRuleResponse(
            Long schemeId,
            Integer minAge,
            Integer maxAge,
            Long minIncome,
            Long maxIncome,
            String gender,
            String category
    ) {}

    public record EligibleSchemeResponse(
            SchemeDtos.SchemeResponse scheme,
            boolean eligible,
            List<String> reasons
    ) {}
}

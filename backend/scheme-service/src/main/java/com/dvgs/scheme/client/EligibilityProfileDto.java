package com.dvgs.scheme.client;

import java.time.LocalDate;

public record EligibilityProfileDto(
        String id,
        LocalDate dateOfBirth,
        Long annualIncome,
        String gender,
        String category
) {}

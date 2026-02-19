package com.dvgs.auth.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EligibilityProfileDto {
    String id;
    LocalDate dateOfBirth;
    Long annualIncome;
    String gender;
    String category;
}

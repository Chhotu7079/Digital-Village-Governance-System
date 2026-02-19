package com.dvgs.scheme.service;

import com.dvgs.scheme.client.AuthClient;
import com.dvgs.scheme.client.EligibilityProfileDto;
import com.dvgs.scheme.domain.Scheme;
import com.dvgs.scheme.domain.SchemeEligibilityRule;
import com.dvgs.scheme.domain.SchemeStatus;
import com.dvgs.scheme.dto.EligibilityDtos;
import com.dvgs.scheme.dto.SchemeDtos;
import com.dvgs.scheme.repository.SchemeEligibilityRuleRepository;
import com.dvgs.scheme.repository.SchemeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final AuthClient authClient;
    private final SchemeRepository schemeRepository;
    private final SchemeEligibilityRuleRepository ruleRepository;
    private final SchemeService schemeService;

    @Transactional
    public EligibilityDtos.EligibilityRuleResponse upsertRule(Long schemeId, EligibilityDtos.UpsertEligibilityRuleRequest req) {
        Scheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new EntityNotFoundException("Scheme not found: " + schemeId));

        SchemeEligibilityRule rule = ruleRepository.findByScheme_Id(schemeId).orElseGet(() -> {
            SchemeEligibilityRule r = new SchemeEligibilityRule();
            r.setScheme(scheme);
            return r;
        });

        rule.setMinAge(req.minAge());
        rule.setMaxAge(req.maxAge());
        rule.setMinIncome(req.minIncome());
        rule.setMaxIncome(req.maxIncome());
        rule.setGender(req.gender());
        rule.setCategory(req.category());

        SchemeEligibilityRule saved = ruleRepository.save(rule);
        return toRuleResponse(saved);
    }

    @Transactional(readOnly = true)
    public EligibilityDtos.EligibilityRuleResponse getRule(Long schemeId) {
        SchemeEligibilityRule rule = ruleRepository.findByScheme_Id(schemeId)
                .orElseThrow(() -> new EntityNotFoundException("Eligibility rule not found for schemeId=" + schemeId));
        return toRuleResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<EligibilityDtos.EligibleSchemeResponse> eligibleSchemes(String bearerToken) {
        EligibilityProfileDto profile = authClient.getMyEligibilityProfile(bearerToken);
        Integer age = computeAge(profile.dateOfBirth());

        // Only ACTIVE schemes should be considered for eligibility
        List<Scheme> activeSchemes = schemeRepository.findByStatus(SchemeStatus.ACTIVE);
        List<SchemeDtos.SchemeResponse> schemes = activeSchemes.stream().map(s -> schemeService.getById(s.getId())).toList();
        List<EligibilityDtos.EligibleSchemeResponse> out = new ArrayList<>();

        for (SchemeDtos.SchemeResponse scheme : schemes) {
            var reasons = new ArrayList<String>();
            var ruleOpt = ruleRepository.findByScheme_Id(scheme.id());
            boolean eligible = true;

            if (ruleOpt.isPresent()) {
                SchemeEligibilityRule r = ruleOpt.get();

                if (r.getMinAge() != null) {
                    if (age == null) {
                        eligible = false;
                        reasons.add("Missing dateOfBirth");
                    } else if (age < r.getMinAge()) {
                        eligible = false;
                        reasons.add("Age below minimum");
                    }
                }
                if (r.getMaxAge() != null) {
                    if (age == null) {
                        eligible = false;
                        reasons.add("Missing dateOfBirth");
                    } else if (age > r.getMaxAge()) {
                        eligible = false;
                        reasons.add("Age above maximum");
                    }
                }
                if (r.getMinIncome() != null) {
                    if (profile.annualIncome() == null) {
                        eligible = false;
                        reasons.add("Missing annualIncome");
                    } else if (profile.annualIncome() < r.getMinIncome()) {
                        eligible = false;
                        reasons.add("Income below minimum");
                    }
                }
                if (r.getMaxIncome() != null) {
                    if (profile.annualIncome() == null) {
                        eligible = false;
                        reasons.add("Missing annualIncome");
                    } else if (profile.annualIncome() > r.getMaxIncome()) {
                        eligible = false;
                        reasons.add("Income above maximum");
                    }
                }
                if (r.getGender() != null && !r.getGender().isBlank()) {
                    if (profile.gender() == null || profile.gender().isBlank()) {
                        eligible = false;
                        reasons.add("Missing gender");
                    } else if (!r.getGender().equalsIgnoreCase(profile.gender())) {
                        eligible = false;
                        reasons.add("Gender not eligible");
                    }
                }
                if (r.getCategory() != null && !r.getCategory().isBlank()) {
                    if (profile.category() == null || profile.category().isBlank()) {
                        eligible = false;
                        reasons.add("Missing category");
                    } else if (!r.getCategory().equalsIgnoreCase(profile.category())) {
                        eligible = false;
                        reasons.add("Category not eligible");
                    }
                }
            }

            out.add(new EligibilityDtos.EligibleSchemeResponse(scheme, eligible, reasons));
        }

        return out;
    }

    private Integer computeAge(LocalDate dob) {
        if (dob == null) {
            return null;
        }
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private EligibilityDtos.EligibilityRuleResponse toRuleResponse(SchemeEligibilityRule rule) {
        return new EligibilityDtos.EligibilityRuleResponse(
                rule.getScheme().getId(),
                rule.getMinAge(),
                rule.getMaxAge(),
                rule.getMinIncome(),
                rule.getMaxIncome(),
                rule.getGender(),
                rule.getCategory()
        );
    }
}

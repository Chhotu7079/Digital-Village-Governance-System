package com.dvgs.auth.service;

import com.dvgs.auth.domain.User;
import com.dvgs.auth.dto.AuthUserView;
import com.dvgs.auth.dto.EligibilityProfileDto;
import com.dvgs.auth.dto.LoginResponse;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AuthUserView toView(User user) {
        return AuthUserView.builder()
                .id(user.getId().toString())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .build();
    }

    public EligibilityProfileDto toEligibilityProfile(User user) {
        return EligibilityProfileDto.builder()
                .id(user.getId().toString())
                .dateOfBirth(user.getDateOfBirth())
                .annualIncome(user.getAnnualIncome())
                .gender(user.getGender())
                .category(user.getCategory())
                .build();
    }

    public LoginResponse toLoginResponse(User user, String accessToken, String refreshToken,
                                         Instant accessExpires, Instant refreshExpires) {
        return LoginResponse.builder()
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessExpires)
                .refreshTokenExpiresAt(refreshExpires)
                .tokenType("Bearer")
                .build();
    }
}

package com.dvgs.auth.dto;

import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthUserView {
    String id;
    String fullName;
    String phoneNumber;
    String email;
    Set<String> roles;
}

package com.dvgs.auth.dto;

import com.dvgs.auth.domain.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;

    @Email
    private String email;

    private String password;

    @NotEmpty
    private Set<RoleType> roles;
}

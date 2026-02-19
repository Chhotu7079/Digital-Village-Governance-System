package com.dvgs.auth.dto;

import com.dvgs.auth.domain.OtpChallenge.OtpChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpRequest {

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phoneNumber;

    private OtpChannel channel = OtpChannel.SMS;
}

package com.dvgs.notification.dto;

import com.dvgs.notification.domain.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record TemplateRequestDto(
        @NotBlank String code,
        @NotNull ChannelType channel,
        @NotBlank String language,
        @NotBlank String title,
        @NotBlank String body,
        Map<String, String> metadata
) {}

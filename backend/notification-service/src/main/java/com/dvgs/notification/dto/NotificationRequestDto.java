package com.dvgs.notification.dto;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record NotificationRequestDto(
        @NotBlank String referenceId,
        @NotBlank String sourceService,
        NotificationPriority priority,
        @NotEmpty List<ChannelType> preferredChannels,
        @NotBlank String templateCode,
        @NotBlank String language,
        Map<String, String> placeholders,
        String userId
) { }

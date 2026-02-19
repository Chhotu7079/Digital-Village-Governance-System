package com.dvgs.notification.dto;

import com.dvgs.notification.domain.ChannelType;
import java.time.LocalTime;

public record ChannelPreferenceRequestDto(
        ChannelType channel,
        boolean enabled,
        ChannelType fallbackChannel,
        LocalTime dndStart,
        LocalTime dndEnd
) {}

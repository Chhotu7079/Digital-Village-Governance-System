package com.dvgs.common.events.dto;

import java.util.List;
import java.util.Map;

/**
 * Shared notification dispatch request payload.
 *
 * NOTE: Keep this contract independent of notification-service internal domain enums to avoid
 * tight coupling. Use Strings for enums.
 */
public record NotificationEventPayload(
        String referenceId,
        String sourceService,
        String priority,
        List<String> preferredChannels,
        String templateCode,
        String language,
        Map<String, String> placeholders,
        String userId
) {}

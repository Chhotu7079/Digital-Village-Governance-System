package com.dvgs.notification.event;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationPriority;
import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.event.dto.SchemeEventPayload;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SchemeEventToNotificationMapper {

    public NotificationRequestDto map(SchemeEventPayload event) {
        String templateCode = switch (event.getType()) {
            case "APPLICATION_SUBMITTED" -> "SCHEME_APPLICATION_SUBMITTED";
            case "APPLICATION_APPROVED" -> "SCHEME_APPLICATION_APPROVED";
            case "APPLICATION_REJECTED" -> "SCHEME_APPLICATION_REJECTED";
            case "APPLICATION_NEED_MORE_INFO" -> "SCHEME_APPLICATION_NEED_MORE_INFO";
            case "APPLICATION_CANCELLED" -> "SCHEME_APPLICATION_CANCELLED";
            default -> null;
        };

        if (templateCode == null) {
            return null;
        }

        String language = "en";

        boolean includeReason = switch (event.getType()) {
            case "APPLICATION_REJECTED", "APPLICATION_NEED_MORE_INFO", "APPLICATION_CANCELLED" -> true;
            default -> false;
        };

        Map<String, String> placeholders = Map.of(
                "applicationId", String.valueOf(event.getApplicationId()),
                "schemeId", String.valueOf(event.getSchemeId()),
                "reason", includeReason ? (event.getDescription() != null ? event.getDescription() : "") : ""
        );

        return new NotificationRequestDto(
                String.valueOf(event.getApplicationId()),
                "scheme-service",
                NotificationPriority.NORMAL,
                List.of(ChannelType.WHATSAPP, ChannelType.SMS),
                templateCode,
                language,
                placeholders,
                event.getApplicantUserId()
        );
    }

}

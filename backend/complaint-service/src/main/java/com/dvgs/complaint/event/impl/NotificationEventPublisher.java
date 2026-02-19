package com.dvgs.complaint.event.impl;

import com.dvgs.complaint.client.NotificationClient;
import com.dvgs.complaint.config.notification.NotificationTemplateProperties;
import com.dvgs.complaint.event.ComplaintEvent;
import com.dvgs.complaint.event.ComplaintEventPublisher;
import com.dvgs.complaint.notification.NotificationPreference;
import com.dvgs.complaint.notification.NotificationPreferenceService;
import com.dvgs.complaint.notification.TemplateRenderer;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher implements ComplaintEventPublisher {

    private final NotificationClient notificationClient;
    private final NotificationPreferenceService preferenceService;
    private final NotificationTemplateProperties templates;
    private final TemplateRenderer templateRenderer;

    @Override
    public void publish(ComplaintEvent event) {
        if (event.getCitizenId() == null) {
            return;
        }
        try {
            NotificationPreference pref = preferenceService.getOrDefault(event.getCitizenId());
            if (!pref.isEnabled()) {
                return;
            }
            NotificationTemplateProperties.Template template = templates.getDefinitions().get(event.getType());
            if (template == null) {
                log.warn("No template for event type {}", event.getType());
                return;
            }
            Map<String, Object> context = new HashMap<>();
            context.put("complaintId", event.getComplaintId());
            context.put("status", event.getStatus());
            context.put("departmentId", event.getDepartmentId());

            String channel = pref.getPreferredChannel().name();
            String language = pref.getLanguage();
            String body = renderBody(template, channel, language, context);
            if (body == null) {
                log.warn("No template body for channel {} and language {}", channel, language);
                return;
            }
            notificationClient.sendNotification(channel, event.getType(), Map.of(
                    "citizenId", event.getCitizenId(),
                    "body", body,
                    "language", language,
                    "timestamp", event.getTimestamp().toString()
            ));
        } catch (Exception ex) {
            log.error("Failed to send complaint notification", ex);
        }
    }

    private String renderBody(NotificationTemplateProperties.Template template, String channel, String language, Map<String, Object> context) {
        Map<String, String> map = channel.equals("WHATSAPP") ? template.getWhatsapp() : template.getSms();
        if (map == null) {
            return null;
        }
        String raw = map.getOrDefault(language, map.get("en"));
        if (raw == null) {
            return null;
        }
        return templateRenderer.render(raw, context);
    }
}

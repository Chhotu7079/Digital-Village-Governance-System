package com.dvgs.complaint.notification;

import java.util.UUID;

public interface NotificationPreferenceService {
    NotificationPreference getOrDefault(UUID citizenId);
    NotificationPreference save(NotificationPreference preference);
}

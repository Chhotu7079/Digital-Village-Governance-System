package com.dvgs.complaint.notification;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;

    @Override
    public NotificationPreference getOrDefault(UUID citizenId) {
        return repository.findById(citizenId).orElseGet(() -> {
            NotificationPreference pref = new NotificationPreference();
            pref.setCitizenId(citizenId);
            return pref;
        });
    }

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        return repository.save(preference);
    }
}

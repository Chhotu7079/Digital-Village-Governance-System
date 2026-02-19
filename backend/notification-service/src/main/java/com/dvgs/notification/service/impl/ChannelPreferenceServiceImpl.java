package com.dvgs.notification.service.impl;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.UserChannelPreference;
import com.dvgs.notification.domain.UserChannelPreferenceId;
import com.dvgs.notification.dto.ChannelPreferenceRequestDto;
import com.dvgs.notification.repository.UserChannelPreferenceRepository;
import com.dvgs.notification.service.ChannelPreferenceService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChannelPreferenceServiceImpl implements ChannelPreferenceService {

    private final UserChannelPreferenceRepository preferenceRepository;

    public ChannelPreferenceServiceImpl(UserChannelPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public List<UserChannelPreference> getPreferences(String userId) {
        return preferenceRepository.findByIdUserId(userId);
    }

    @Override
    public List<UserChannelPreference> updatePreferences(String userId, List<ChannelPreferenceRequestDto> requests) {
        for (ChannelPreferenceRequestDto request : requests) {
            ChannelType channel = Objects.requireNonNullElse(request.channel(), ChannelType.SMS);
            UserChannelPreferenceId id = new UserChannelPreferenceId(userId, channel);
            UserChannelPreference preference = preferenceRepository.findById(id)
                    .orElseGet(() -> {
                        UserChannelPreference p = new UserChannelPreference();
                        p.setId(id);
                        return p;
                    });
            preference.setEnabled(request.enabled());
            preference.setFallbackChannel(request.fallbackChannel());
            preference.setDndStart(request.dndStart());
            preference.setDndEnd(request.dndEnd());
            preferenceRepository.save(preference);
        }
        return preferenceRepository.findByIdUserId(userId);
    }
}

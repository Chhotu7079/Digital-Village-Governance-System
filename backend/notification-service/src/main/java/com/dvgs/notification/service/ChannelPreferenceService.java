package com.dvgs.notification.service;

import com.dvgs.notification.domain.UserChannelPreference;
import com.dvgs.notification.dto.ChannelPreferenceRequestDto;
import java.util.List;

public interface ChannelPreferenceService {
    List<UserChannelPreference> getPreferences(String userId);
    List<UserChannelPreference> updatePreferences(String userId, List<ChannelPreferenceRequestDto> requests);
}

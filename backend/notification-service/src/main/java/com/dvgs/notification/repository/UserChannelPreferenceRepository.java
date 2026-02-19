package com.dvgs.notification.repository;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.UserChannelPreference;
import com.dvgs.notification.domain.UserChannelPreferenceId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChannelPreferenceRepository extends JpaRepository<UserChannelPreference, UserChannelPreferenceId> {
    List<UserChannelPreference> findByIdUserId(String userId);
    List<UserChannelPreference> findByIdUserIdAndEnabledTrue(String userId);
    boolean existsByIdUserIdAndIdChannelAndEnabledTrue(String userId, ChannelType channelType);
}

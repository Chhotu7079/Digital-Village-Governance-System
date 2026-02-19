package com.dvgs.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_channel_preferences")
@Getter
@Setter
public class UserChannelPreference {

    @EmbeddedId
    private UserChannelPreferenceId id;

    @Column(nullable = false)
    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private ChannelType fallbackChannel;

    private LocalTime dndStart;
    private LocalTime dndEnd;
}

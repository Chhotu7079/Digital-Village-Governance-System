package com.dvgs.complaint.notification;

import com.dvgs.complaint.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
public class NotificationPreference extends AuditableEntity {

    @Id
    @Column(name = "citizen_id")
    private UUID citizenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel preferredChannel = Channel.SMS;

    @Column(name = "language", nullable = false)
    private String language = "en";

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    public enum Channel {
        SMS,
        WHATSAPP
    }
}

package com.dvgs.notification.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_requests")
@Getter
@Setter
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String sourceService;

    @Column(nullable = false)
    private String referenceId;

    @Column(nullable = false)
    private String templateCode;

    @Column(nullable = false)
    private String language;

    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationPriority priority;

    @ElementCollection
    @CollectionTable(name = "notification_request_channels", joinColumns = @JoinColumn(name = "request_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "channel")
    private Set<ChannelType> preferredChannels;

    @ElementCollection
    @CollectionTable(name = "notification_request_payload", joinColumns = @JoinColumn(name = "request_id"))
    @MapKeyColumn(name = "payload_key")
    @Column(name = "value")
    private Map<String, String> payload;

    private OffsetDateTime createdAt;
}

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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notification_templates")
@Getter
@Setter
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String body;

    @ElementCollection
    @CollectionTable(name = "notification_template_metadata", joinColumns = @JoinColumn(name = "template_id"))
    @jakarta.persistence.MapKeyColumn(name = "metadata_key")
    @Column(name = "value")
    private Map<String, String> metadata;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

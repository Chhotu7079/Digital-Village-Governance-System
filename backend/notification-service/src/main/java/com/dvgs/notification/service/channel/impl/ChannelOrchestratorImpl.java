package com.dvgs.notification.service.channel.impl;

import com.dvgs.notification.config.properties.NotificationProperties;
import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.domain.NotificationLog;
import com.dvgs.notification.domain.NotificationPriority;
import com.dvgs.notification.domain.NotificationStatus;
import com.dvgs.notification.domain.UserChannelPreference;
import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.event.NotificationEventPublisher;
import com.dvgs.notification.metrics.NotificationMetricsPublisher;
import com.dvgs.notification.repository.NotificationLogRepository;
import com.dvgs.notification.repository.NotificationTemplateRepository;
import com.dvgs.notification.repository.UserChannelPreferenceRepository;
import com.dvgs.notification.service.channel.ChannelAdapter;
import com.dvgs.notification.service.channel.ChannelDispatchContext;
import com.dvgs.notification.service.channel.ChannelOrchestrator;
import com.dvgs.notification.util.TemplateRenderer;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ChannelOrchestratorImpl implements ChannelOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ChannelOrchestratorImpl.class);

    private final Map<ChannelType, ChannelAdapter> adapters;
    private final TemplateRenderer templateRenderer;
    private final UserChannelPreferenceRepository preferenceRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final NotificationEventPublisher eventPublisher;
    private final NotificationProperties notificationProperties;
    private final NotificationMetricsPublisher metricsPublisher;

    public ChannelOrchestratorImpl(List<ChannelAdapter> adapters,
                                   TemplateRenderer templateRenderer,
                                   UserChannelPreferenceRepository preferenceRepository,
                                   NotificationTemplateRepository templateRepository,
                                   NotificationLogRepository logRepository,
                                   NotificationEventPublisher eventPublisher,
                                   NotificationProperties notificationProperties,
                                   NotificationMetricsPublisher metricsPublisher) {
        this.adapters = adapters.stream().collect(Collectors.toMap(ChannelAdapter::getChannelType, adapter -> adapter));
        this.templateRenderer = templateRenderer;
        this.preferenceRepository = preferenceRepository;
        this.templateRepository = templateRepository;
        this.logRepository = logRepository;
        this.eventPublisher = eventPublisher;
        this.notificationProperties = notificationProperties;
        this.metricsPublisher = metricsPublisher;
    }

    @Override
    public void process(NotificationRequestDto requestDto, String requestId) {
        List<UserChannelPreference> preferences = requestDto.userId() != null
                ? preferenceRepository.findByIdUserIdAndEnabledTrue(requestDto.userId())
                : List.of();

        LinkedHashSet<ChannelType> channelsToTry = new LinkedHashSet<>(requestDto.preferredChannels());
        Set<ChannelType> attempted = new HashSet<>();
        boolean delivered = false;
        NotificationPriority priority = requestDto.priority() != null ? requestDto.priority() : NotificationPriority.NORMAL;

        while (!channelsToTry.isEmpty() && !delivered) {
            ChannelType channel = channelsToTry.iterator().next();
            channelsToTry.remove(channel);
            attempted.add(channel);

            ChannelEligibility eligibility = evaluateChannel(preferences, channel, priority);
            if (!eligibility.allowed()) {
                log.debug("Skipping channel {} for request {}: {}", channel, requestId, eligibility.reason());
                metricsPublisher.recordPreferenceSkip(channel, eligibility.reason());
                continue;
            }

            delivered = sendOnChannel(requestDto, requestId, channel);

            if (!delivered) {
                addFallbackChannels(channelsToTry, attempted);
            }
        }

        if (!delivered) {
            log.warn("Request {} failed on all channels", requestId);
        }
    }

    private ChannelEligibility evaluateChannel(List<UserChannelPreference> preferences,
                                                ChannelType channel,
                                                NotificationPriority priority) {
        if (!notificationProperties.isChannelEnabled(channel)) {
            return new ChannelEligibility(false, "Channel disabled in config");
        }

        for (UserChannelPreference preference : preferences) {
            if (!preference.getId().getChannel().equals(channel)) {
                continue;
            }
            if (!preference.isEnabled()) {
                return new ChannelEligibility(false, "User disabled channel");
            }
            if (isWithinDnd(preference) && priority != NotificationPriority.CRITICAL) {
                return new ChannelEligibility(false, "User in DND window");
            }
        }
        return new ChannelEligibility(true, "allowed");
    }

    private boolean isWithinDnd(UserChannelPreference preference) {
        if (preference.getDndStart() == null || preference.getDndEnd() == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        LocalTime start = preference.getDndStart();
        LocalTime end = preference.getDndEnd();
        if (start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        // window spans midnight
        return !now.isBefore(start) || now.isBefore(end);
    }

    private void addFallbackChannels(Set<ChannelType> channelsToTry, Set<ChannelType> attempted) {
        for (ChannelType fallback : notificationProperties.getFallbackOrder()) {
            if (!attempted.contains(fallback) && !channelsToTry.contains(fallback)) {
                channelsToTry.add(fallback);
            }
        }
    }

    private String resolveDestination(ChannelType channel, NotificationRequestDto requestDto) {
        Map<String, String> placeholders = requestDto.placeholders();
        if (placeholders == null) {
            return null;
        }
        return switch (channel) {
            case SMS, WHATSAPP -> placeholders.getOrDefault("phone", null);
            case EMAIL -> placeholders.getOrDefault("email", null);
            case PUSH -> placeholders.getOrDefault("deviceToken", null);
        };
    }

    private boolean sendOnChannel(NotificationRequestDto requestDto, String requestId, ChannelType channel) {
        NotificationLog logEntry = new NotificationLog();
        logEntry.setRequestId(java.util.UUID.fromString(requestId));
        logEntry.setChannel(channel);
        logEntry.setStatus(NotificationStatus.QUEUED);
        logEntry.setAttemptCount(0);
        logEntry.setCreatedAt(OffsetDateTime.now());
        logRepository.save(logEntry);

        ChannelAdapter adapter = adapters.get(channel);
        if (adapter == null) {
            log.warn("No adapter configured for channel {}", channel);
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorDescription("Adapter missing");
            logEntry.setLastAttemptAt(OffsetDateTime.now());
            logRepository.save(logEntry);
            metricsPublisher.recordDispatchOutcome(requestId, channel, logEntry.getStatus(), logEntry.getCreatedAt(), logEntry.getLastAttemptAt());
            return false;
        }

        return templateRepository.findByCodeAndChannelAndLanguage(requestDto.templateCode(), channel, requestDto.language())
                .map(template -> {
                    String body = templateRenderer.render(template.getBody(), requestDto.placeholders());
                    ChannelDispatchContext context = new ChannelDispatchContext(
                            requestId,
                            channel,
                            requestDto,
                            template.getTitle(),
                            body,
                            template.getMetadata(),
                            notificationProperties.getChannelSetting(channel),
                            resolveDestination(channel, requestDto)
                    );
                    try {
                        String providerMessageId = adapter.send(context);
                        logEntry.setStatus(NotificationStatus.SENT);
                        logEntry.setProviderMessageId(providerMessageId);
                        return true;
                    } catch (Exception ex) {
                        log.error("Failed to send notification {} on channel {}", requestId, channel, ex);
                        logEntry.setStatus(NotificationStatus.FAILED);
                        logEntry.setErrorDescription(ex.getMessage());
                        return false;
                    } finally {
                        logEntry.setAttemptCount(logEntry.getAttemptCount() + 1);
                        logEntry.setLastAttemptAt(OffsetDateTime.now());
                        NotificationLog persisted = logRepository.save(logEntry);
                        eventPublisher.publishDeliveryEvent(persisted);
                        metricsPublisher.recordDispatchOutcome(
                                requestId,
                                channel,
                                logEntry.getStatus(),
                                logEntry.getCreatedAt(),
                                logEntry.getLastAttemptAt());
                    }
                })
                .orElseGet(() -> {
                    log.error("Template {} for channel {} language {} not found", requestDto.templateCode(), channel, requestDto.language());
                    logEntry.setStatus(NotificationStatus.FAILED);
                    logEntry.setErrorDescription("Template missing");
                    logEntry.setLastAttemptAt(OffsetDateTime.now());
                    logRepository.save(logEntry);
                    metricsPublisher.recordDispatchOutcome(
                            requestId,
                            channel,
                            logEntry.getStatus(),
                            logEntry.getCreatedAt(),
                            logEntry.getLastAttemptAt());
                    return false;
                });
    }

    private record ChannelEligibility(boolean allowed, String reason) {}
}

package com.dvgs.notification.service.impl;

import com.dvgs.notification.domain.NotificationPriority;
import com.dvgs.notification.domain.NotificationRequest;
import com.dvgs.notification.dto.NotificationRequestDto;
import com.dvgs.notification.dto.NotificationStatusDto;
import com.dvgs.notification.metrics.NotificationMetricsPublisher;
import com.dvgs.notification.repository.NotificationLogRepository;
import com.dvgs.notification.repository.NotificationRequestRepository;
import com.dvgs.notification.service.NotificationDispatchService;
import com.dvgs.notification.service.channel.ChannelOrchestrator;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final NotificationRequestRepository requestRepository;
    private final NotificationLogRepository logRepository;
    private final ChannelOrchestrator channelOrchestrator;
    private final NotificationMetricsPublisher metricsPublisher;

    public NotificationDispatchServiceImpl(NotificationRequestRepository requestRepository,
                                           NotificationLogRepository logRepository,
                                           ChannelOrchestrator channelOrchestrator,
                                           NotificationMetricsPublisher metricsPublisher) {
        this.requestRepository = requestRepository;
        this.logRepository = logRepository;
        this.channelOrchestrator = channelOrchestrator;
        this.metricsPublisher = metricsPublisher;
    }

    @Override
    public String enqueueNotification(NotificationRequestDto requestDto) {
        NotificationRequest request = new NotificationRequest();
        UUID requestId = UUID.randomUUID();
        request.setId(requestId);
        request.setSourceService(requestDto.sourceService());
        request.setReferenceId(requestDto.referenceId());
        request.setPriority(requestDto.priority() != null ? requestDto.priority() : NotificationPriority.NORMAL);
        request.setTemplateCode(requestDto.templateCode());
        request.setLanguage(requestDto.language());
        request.setUserId(requestDto.userId());
        request.setPreferredChannels(new HashSet<>(requestDto.preferredChannels()));
        request.setPayload(requestDto.placeholders() != null ? new HashMap<>(requestDto.placeholders()) : Map.of());
        request.setCreatedAt(OffsetDateTime.now());
        requestRepository.save(request);

        metricsPublisher.recordSubmission(requestDto.sourceService(), request.getPriority().name());

        // delegate to orchestrator for async processing (can be replaced with actual queue)
        channelOrchestrator.process(requestDto, requestId.toString());

        return requestId.toString();
    }

    @Override
    public List<NotificationStatusDto> getStatus(String requestId) {
        metricsPublisher.recordStatusLookup();
        return logRepository.findByRequestIdOrderByLastAttemptAtDesc(UUID.fromString(requestId))
                .stream()
                .map(log -> new NotificationStatusDto(
                        log.getChannel(),
                        log.getStatus(),
                        log.getAttemptCount(),
                        log.getProviderMessageId(),
                        log.getLastAttemptAt()
                )).toList();
    }
}

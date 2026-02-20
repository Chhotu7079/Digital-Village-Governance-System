package com.dvgs.notification.service.channel.impl.adapters;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.service.channel.ChannelAdapter;
import com.dvgs.notification.service.channel.ChannelDispatchContext;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "notification.channels.push", name = "enabled", havingValue = "true")
public class PushChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(PushChannelAdapter.class);
    private final FirebaseMessaging firebaseMessaging;

    public PushChannelAdapter(FirebaseApp firebaseApp) {
        this.firebaseMessaging = (firebaseApp == null) ? null : FirebaseMessaging.getInstance(firebaseApp);
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.PUSH;
    }

    @Override
    public String send(ChannelDispatchContext context) {
        String token = context.destination();
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing device token for push notification");
        }

        if (firebaseMessaging == null) {
            throw new IllegalStateException("Firebase is not configured (FIREBASE_SERVICE_ACCOUNT_JSON is missing)");
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(context.title())
                        .setBody(context.body())
                        .build())
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("Sent push notification to token {} for request {}", token, context.requestId());
            return response;
        } catch (com.google.firebase.messaging.FirebaseMessagingException ex) {
            log.error("Failed to send push notification for request {}", context.requestId(), ex);
            throw new IllegalStateException("Failed to send push notification", ex);
        }
    }
}

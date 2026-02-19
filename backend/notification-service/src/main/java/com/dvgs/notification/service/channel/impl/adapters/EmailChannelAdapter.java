package com.dvgs.notification.service.channel.impl.adapters;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.service.channel.ChannelAdapter;
import com.dvgs.notification.service.channel.ChannelDispatchContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
public class EmailChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(EmailChannelAdapter.class);
    private final SesClient sesClient;

    public EmailChannelAdapter(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    @Override
    public ChannelType getChannelType() {
        return ChannelType.EMAIL;
    }

    @Override
    public String send(ChannelDispatchContext context) {
        String to = context.destination();
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Missing email destination");
        }
        String from = context.channelSetting().getFrom();
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(to).build())
                .message(Message.builder()
                        .subject(Content.builder().data(context.title()).build())
                        .body(Body.builder().text(Content.builder().data(context.body()).build()).build())
                        .build())
                .source(from)
                .build();
        sesClient.sendEmail(request);
        log.info("Sent email to {} for request {}", to, context.requestId());
        return "email-" + context.request().referenceId();
    }
}

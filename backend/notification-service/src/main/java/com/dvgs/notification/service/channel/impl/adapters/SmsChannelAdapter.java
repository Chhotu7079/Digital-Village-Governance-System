package com.dvgs.notification.service.channel.impl.adapters;

import com.dvgs.notification.domain.ChannelType;
import com.dvgs.notification.service.channel.ChannelAdapter;
import com.dvgs.notification.service.channel.ChannelDispatchContext;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SmsChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(SmsChannelAdapter.class);

    @Override
    public ChannelType getChannelType() {
        return ChannelType.SMS;
    }

    @Override
    public String send(ChannelDispatchContext context) {
        String to = context.destination();
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Missing phone destination for SMS");
        }
        String from = context.channelSetting().getFrom();
        try {
            Message message = Message.creator(new PhoneNumber(to), new PhoneNumber(from), context.body()).create();
            log.info("Sent SMS to {} for request {}", to, context.requestId());
            return message.getSid();
        } catch (ApiException ex) {
            log.error("Twilio SMS send failed for {}", context.requestId(), ex);
            throw ex;
        }
    }
}

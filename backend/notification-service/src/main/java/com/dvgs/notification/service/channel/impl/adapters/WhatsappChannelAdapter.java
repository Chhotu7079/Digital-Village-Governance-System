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
public class WhatsappChannelAdapter implements ChannelAdapter {

    private static final Logger log = LoggerFactory.getLogger(WhatsappChannelAdapter.class);

    @Override
    public ChannelType getChannelType() {
        return ChannelType.WHATSAPP;
    }

    @Override
    public String send(ChannelDispatchContext context) {
        String to = context.destination();
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Missing phone destination for WhatsApp");
        }
        String from = "whatsapp:" + context.channelSetting().getNumber();
        String toWhatsApp = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
        try {
            Message message = Message.creator(new PhoneNumber(toWhatsApp), new PhoneNumber(from), context.body()).create();
            log.info("Sent WhatsApp message to {} for request {}", toWhatsApp, context.requestId());
            return message.getSid();
        } catch (ApiException ex) {
            log.error("Twilio WhatsApp send failed for {}", context.requestId(), ex);
            throw ex;
        }
    }
}

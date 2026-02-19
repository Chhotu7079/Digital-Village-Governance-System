package com.dvgs.notification.service.channel;

import com.dvgs.notification.domain.ChannelType;

public interface ChannelAdapter {
    ChannelType getChannelType();
    String send(ChannelDispatchContext context);
}

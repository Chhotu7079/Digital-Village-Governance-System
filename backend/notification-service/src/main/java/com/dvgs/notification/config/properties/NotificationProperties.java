package com.dvgs.notification.config.properties;

import com.dvgs.notification.domain.ChannelType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private List<ChannelType> fallbackOrder = new ArrayList<>();
    private final Map<ChannelType, ChannelSetting> channels = new EnumMap<>(ChannelType.class);

    public List<ChannelType> getFallbackOrder() {
        return fallbackOrder;
    }

    public void setFallbackOrder(List<ChannelType> fallbackOrder) {
        this.fallbackOrder = fallbackOrder;
    }

    public Map<ChannelType, ChannelSetting> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, ChannelSetting> channelConfig) {
        channels.clear();
        if (channelConfig != null) {
            channelConfig.forEach((key, value) -> channels.put(ChannelType.valueOf(key.toUpperCase()), value));
        }
    }

    public boolean isChannelEnabled(ChannelType type) {
        ChannelSetting setting = channels.get(type);
        return setting == null || setting.isEnabled();
    }

    public ChannelSetting getChannelSetting(ChannelType type) {
        return channels.getOrDefault(type, new ChannelSetting());
    }

    public static class ChannelSetting {
        private boolean enabled = true;
        private String provider;
        private String from;
        private String number;
        private String accountSid;
        private String authToken;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getAccountSid() {
            return accountSid;
        }

        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }
    }
}

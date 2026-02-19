package com.dvgs.complaint.config.notification;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notification.templates")
public class NotificationTemplateProperties {

    private Map<String, Template> definitions;

    public Map<String, Template> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(Map<String, Template> definitions) {
        this.definitions = definitions;
    }

    public static class Template {
        private Map<String, String> sms;
        private Map<String, String> whatsapp;

        public Map<String, String> getSms() {
            return sms;
        }

        public void setSms(Map<String, String> sms) {
            this.sms = sms;
        }

        public Map<String, String> getWhatsapp() {
            return whatsapp;
        }

        public void setWhatsapp(Map<String, String> whatsapp) {
            this.whatsapp = whatsapp;
        }
    }
}

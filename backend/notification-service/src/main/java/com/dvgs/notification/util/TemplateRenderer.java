package com.dvgs.notification.util;

import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class TemplateRenderer {

    public String render(String template, Map<String, String> placeholders) {
        if (template == null) {
            return "";
        }
        return StringSubstitutor.replace(template, placeholders != null ? placeholders : Map.of(), "${", "}");
    }
}

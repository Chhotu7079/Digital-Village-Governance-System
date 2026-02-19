package com.dvgs.complaint.notification;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TemplateRenderer {

    public String render(String template, Map<String, Object> context) {
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return result;
    }
}

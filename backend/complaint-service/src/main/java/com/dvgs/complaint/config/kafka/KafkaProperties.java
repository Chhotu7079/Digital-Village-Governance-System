package com.dvgs.complaint.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";
    private String complaintTopic = "complaint-events";

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getComplaintTopic() {
        return complaintTopic;
    }

    public void setComplaintTopic(String complaintTopic) {
        this.complaintTopic = complaintTopic;
    }
}

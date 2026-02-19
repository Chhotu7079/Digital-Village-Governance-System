package com.dvgs.scheme.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {

    private String bootstrapServers = "localhost:9092";
    private String schemeTopic = "scheme-events";

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSchemeTopic() {
        return schemeTopic;
    }

    public void setSchemeTopic(String schemeTopic) {
        this.schemeTopic = schemeTopic;
    }
}

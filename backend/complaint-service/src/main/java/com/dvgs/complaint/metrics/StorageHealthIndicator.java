package com.dvgs.complaint.metrics;

import com.dvgs.complaint.config.storage.StorageProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageProperties properties;

    public StorageHealthIndicator(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            Path path = Path.of(properties.getBasePath());
            boolean writable = Files.isWritable(path);
            long free = Files.getFileStore(path).getUsableSpace();
            return Health.up()
                    .withDetail("path", path.toAbsolutePath().toString())
                    .withDetail("writable", writable)
                    .withDetail("freeBytes", free)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex).build();
        }
    }
}

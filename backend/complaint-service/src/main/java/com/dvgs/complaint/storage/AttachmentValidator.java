package com.dvgs.complaint.storage;

import com.dvgs.complaint.config.storage.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AttachmentValidator {

    private final StorageProperties properties;
    private final Tika tika = new Tika();

    public AttachmentValidator(StorageProperties properties) {
        this.properties = properties;
    }

    public void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("File exceeds max size limit");
        }
        String detectedType = detectMimeType(file);
        Set<String> allowed = Set.of(properties.getAllowedMimeTypes());
        if (!allowed.contains(detectedType)) {
            throw new IllegalArgumentException("Unsupported file type: " + detectedType);
        }
    }

    private String detectMimeType(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return tika.detect(in, file.getOriginalFilename());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to inspect file", e);
        }
    }
}

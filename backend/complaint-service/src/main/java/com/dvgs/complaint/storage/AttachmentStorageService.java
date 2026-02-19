package com.dvgs.complaint.storage;

import com.dvgs.complaint.config.storage.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentStorageService {

    private final StorageProperties properties;
    private final AttachmentValidator validator;
    private final AttachmentScanner scanner;

    public AttachmentStorageService(StorageProperties properties,
                                    AttachmentValidator validator,
                                    AttachmentScanner scanner) {
        this.properties = properties;
        this.validator = validator;
        this.scanner = scanner;
        initDirectory();
    }

    private void initDirectory() {
        try {
            Files.createDirectories(Paths.get(properties.getBasePath()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create attachment directory", e);
        }
    }

    public StoredAttachment store(MultipartFile file) {
        validator.validate(file);
        scanner.scan(file);
        String extension = extractExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path destination = Paths.get(properties.getBasePath()).resolve(storedName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store attachment", e);
        }
        return new StoredAttachment(storedName, destination.toString());
    }

    public byte[] load(String storagePath) {
        try {
            return Files.readAllBytes(Path.of(storagePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read attachment", e);
        }
    }

    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete attachment", e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx == -1 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1);
    }

    public record StoredAttachment(String storedName, String storagePath) {}
}

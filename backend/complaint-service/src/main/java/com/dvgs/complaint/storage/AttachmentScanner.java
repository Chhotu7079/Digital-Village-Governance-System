package com.dvgs.complaint.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AttachmentScanner {
    void scan(MultipartFile file);
}

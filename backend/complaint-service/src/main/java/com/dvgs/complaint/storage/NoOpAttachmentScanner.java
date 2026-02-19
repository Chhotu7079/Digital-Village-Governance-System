package com.dvgs.complaint.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class NoOpAttachmentScanner implements AttachmentScanner {

    @Override
    public void scan(MultipartFile file) {
        // Placeholder for real AV integration (ClamAV, VirusTotal, etc.)
    }
}

package com.dvgs.scheme.attachments;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class AttachmentIntegrityService {

    private static final String FILE_REF_PREFIX = "minio://";

    private final MinioProperties properties;
    private final S3Client s3Client;

    public boolean exists(String fileRef) {
        ParsedFileRef parsed = parseAndValidate(fileRef)
                .orElseThrow(() -> new IllegalArgumentException("Invalid fileRef"));

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(parsed.bucket())
                    .key(parsed.key())
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            // Some S3-compatible servers return 404/NoSuchKey as S3Exception
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    private Optional<ParsedFileRef> parseAndValidate(String fileRef) {
        if (fileRef == null || fileRef.isBlank() || !fileRef.startsWith(FILE_REF_PREFIX)) {
            return Optional.empty();
        }
        String withoutPrefix = fileRef.substring(FILE_REF_PREFIX.length());
        int slash = withoutPrefix.indexOf('/');
        if (slash <= 0 || slash == withoutPrefix.length() - 1) {
            return Optional.empty();
        }
        String bucket = withoutPrefix.substring(0, slash);
        String key = withoutPrefix.substring(slash + 1);

        if (!bucket.equals(properties.getBucket())) {
            throw new IllegalArgumentException("Invalid bucket in fileRef");
        }

        return Optional.of(new ParsedFileRef(bucket, key));
    }

    private record ParsedFileRef(String bucket, String key) {}
}

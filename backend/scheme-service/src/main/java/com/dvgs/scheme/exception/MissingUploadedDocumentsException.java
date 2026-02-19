package com.dvgs.scheme.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class MissingUploadedDocumentsException extends RuntimeException {

    private final Long applicationId;
    private final List<String> missingDocTypes;

    public MissingUploadedDocumentsException(Long applicationId, List<String> missingDocTypes) {
        super("Some uploaded documents are missing in storage for applicationId=" + applicationId + ": " + missingDocTypes);
        this.applicationId = applicationId;
        this.missingDocTypes = missingDocTypes;
    }
}

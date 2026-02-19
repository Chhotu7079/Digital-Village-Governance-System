package com.dvgs.scheme.exception;

import java.util.List;

public class MissingRequiredDocumentsException extends RuntimeException {

    private final List<String> missingDocTypes;

    public MissingRequiredDocumentsException(Long schemeId, List<String> missingDocTypes) {
        super("Missing required documents for schemeId=" + schemeId + ": " + missingDocTypes);
        this.missingDocTypes = missingDocTypes;
    }

    public List<String> getMissingDocTypes() {
        return missingDocTypes;
    }
}

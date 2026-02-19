package com.dvgs.scheme.exception;

import com.dvgs.scheme.domain.ApplicationStatus;

public class ActiveApplicationExistsException extends RuntimeException {

    private final Long existingApplicationId;
    private final ApplicationStatus existingStatus;

    public ActiveApplicationExistsException(Long schemeId, Long existingApplicationId, ApplicationStatus existingStatus) {
        super("Active application already exists for schemeId=" + schemeId + ": applicationId=" + existingApplicationId + ", status=" + existingStatus);
        this.existingApplicationId = existingApplicationId;
        this.existingStatus = existingStatus;
    }

    public Long getExistingApplicationId() {
        return existingApplicationId;
    }

    public ApplicationStatus getExistingStatus() {
        return existingStatus;
    }
}

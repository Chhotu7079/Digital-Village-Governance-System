package com.dvgs.scheme.exception;

import lombok.Getter;

@Getter
public class SchemeArchivedException extends RuntimeException {

    private final Long schemeId;

    public SchemeArchivedException(Long schemeId) {
        super("Scheme is archived: " + schemeId);
        this.schemeId = schemeId;
    }
}

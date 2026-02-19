package com.dvgs.common;

/**
 * Shared request context constants across DVGS services.
 */
public final class RequestContext {

    private RequestContext() {}

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_ATTR = "requestId";
}

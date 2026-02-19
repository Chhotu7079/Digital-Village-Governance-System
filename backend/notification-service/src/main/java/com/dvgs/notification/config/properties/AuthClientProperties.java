package com.dvgs.notification.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth-client")
public class AuthClientProperties {

    /**
     * JWKS endpoint exposed by Auth Service (preferred).
     */
    private String jwkSetUri;

    /**
     * Shared HMAC secret fallback when JWKS is not available.
     */
    private String sharedSecret;

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
}

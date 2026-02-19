package com.dvgs.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final SecurityErrorHandlers errorHandlers;

    public SecurityConfig(SecurityErrorHandlers errorHandlers) {
        this.errorHandlers = errorHandlers;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Auth endpoints are public
                        .pathMatchers("/api/auth/**").permitAll()
                        // Swagger of gateway itself (if added later)
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        // Protect API surface; non-/api paths can remain public (will typically 404)
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll()
                )
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(errorHandlers.authenticationEntryPoint())
                        .accessDeniedHandler(errorHandlers.accessDeniedHandler())
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}))
                .build();
    }
}

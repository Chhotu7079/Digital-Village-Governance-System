package com.dvgs.admin;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminServerProperties adminProps) throws Exception {
        String adminContextPath = adminProps.getContextPath();

        http
                // SBA uses cookies + csrf; keep csrf enabled but ignore SBA's instance registration endpoints.
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        adminContextPath + "/instances",
                        adminContextPath + "/instances/*",
                        adminContextPath + "/actuator/**"))
                .authorizeHttpRequests(auth -> auth
                        // static assets
                        .requestMatchers(adminContextPath + "/assets/**").permitAll()
                        // login page
                        .requestMatchers(adminContextPath + "/login").permitAll()
                        // allow client registration (POST /instances)
                        .requestMatchers(HttpMethod.POST, adminContextPath + "/instances").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage(adminContextPath + "/login").permitAll())
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl(adminContextPath + "/logout"));

        return http.build();
    }
}

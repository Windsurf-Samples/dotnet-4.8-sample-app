package com.descope.sample.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Spring Boot application.
 * This replaces the authentication handling from the .NET TokenValidator
 * and integrates with Spring Security's OAuth2 Resource Server.
 * 
 * Migrated from: DescopeSampleApp/TokenValidator.cs
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${descope.project-id}")
    private String descopeProjectId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/", "/login", "/login.html", "/authenticated.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/static/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                // Protected API endpoints require JWT authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("https://api.descope.com/" + descopeProjectId + "/.well-known/jwks.json")
                )
            );

        return http.build();
    }
}

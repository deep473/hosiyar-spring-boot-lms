package com.hosiyar.lms.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Lets a browser front end served from a different origin call this API.
 *
 * Browsers refuse cross-origin requests unless the server says otherwise, and
 * a page on localhost:5500 calling localhost:8080 counts as cross-origin - a
 * different port is a different origin. Without this, every fetch from the
 * demo UI fails before it reaches a controller.
 *
 * Origins are listed explicitly rather than using "*", because credentials
 * are allowed and the two cannot be combined.
 */
@Configuration
public class CorsConfig {

    @Value("${lms.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

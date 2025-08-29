package com.timelogic.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOriginsCsv;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethodsCsv;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeadersCsv;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim).toList();

        if (allowCredentials && origins.contains("*")) {
            throw new IllegalStateException("CORS: '*' no es compatible con allowCredentials=true");
        }

        // si necesitas comodines usa patterns:
        if (origins.stream().anyMatch(s -> s.contains("*"))) {
            config.setAllowedOriginPatterns(origins);
        } else {
            config.setAllowedOrigins(origins);
        }

        config.setAllowedMethods(Arrays.stream(allowedMethodsCsv.split(",")).map(String::trim).toList());
        config.setAllowedHeaders(Arrays.stream(allowedHeadersCsv.split(",")).map(String::trim).toList());
        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

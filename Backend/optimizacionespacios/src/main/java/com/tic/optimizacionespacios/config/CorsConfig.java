package com.tic.optimizacionespacios.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Permite que el frontend React (en localhost:3000 o :5173)
 * pueda hacer peticiones al backend (localhost:8080).
 *
 * Sin esto, el navegador bloquea las peticiones por política CORS.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos (leídos desde application.properties)
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Métodos HTTP que acepta el backend
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Permite cualquier header en la petición
        config.setAllowedHeaders(List.of("*"));

        // Permite cookies y credenciales (útil si después agregas autenticación)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}

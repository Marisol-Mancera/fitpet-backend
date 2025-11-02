package Marisol_Mancera.fitpet.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración CORS para permitir comunicación Frontend-Backend.
 * 
 * - Permite solicitudes desde http://localhost:5173 (Vite dev server).
 * - Habilita credenciales (cookies, Authorization headers).
 * - Expone cabecera Location para endpoints POST con 201 Created.
 * 
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite el origen del frontend (Vite dev server por defecto en puerto 5173)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // Permite todos los métodos HTTP necesarios para la API REST
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Permite todos los headers comunes (Authorization para JWT, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Habilita el envío de credenciales (importante para JWT en Authorization header)
        configuration.setAllowCredentials(true);
        
        // Expone la cabecera Location (necesaria para POST /api/v1/pets que devuelve 201 + Location)
        configuration.setExposedHeaders(List.of("Location"));
        
        // Aplica esta configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
package Marisol_Mancera.fitpet.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración CORS 
 * Permite que el frontend (localhost:5173) pueda comunicarse con el backend (localhost:8080).
 */
@Configuration
public class CorsConfig {

    /**
     * Configuración CORS para desarrollo
     * 
     * Orígenes permitidos:
     * - http://localhost:5173 (Vite - desarrollo)
     * - http://localhost:3000 (Alternativo - React)
     * 
     * Métodos permitidos:
     * - GET, POST, PUT, DELETE, OPTIONS
     * 
     * Headers permitidos:
     * - Authorization (JWT)
     * - Content-Type
     * - Accept
     * - Origin
     * 
     * IMPORTANTE: En producción, reemplazar localhost con el dominio real.
     * 
     * @return CorsConfigurationSource configurado para desarrollo
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",  // Vite (desarrollo)
            "http://localhost:3000"   // Alternativo
            // Agregar dominio de producción (cuando se despliegue)
            // "https://fitpet.app"
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS"  // Preflight requests
        ));
        
        // Headers permitidos en requests
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",  // JWT token
            "Content-Type",   // application/json
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers expuestos en responses (visibles para el frontend)
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type"
        ));
        
        // Permitir credenciales (cookies, JWT en headers)
        configuration.setAllowCredentials(true);
        
        // Tiempo de cache para preflight requests (1 hora)
        configuration.setMaxAge(3600L);
        
        // Registrar configuración para todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
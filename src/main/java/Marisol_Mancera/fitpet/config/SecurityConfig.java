package Marisol_Mancera.fitpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad con JWT
 * 
 * Responsabilidades:
 * - Autenticación y autorización
 * - Configuración de endpoints públicos/protegidos
 * - Integración con OAuth2 Resource Server (JWT)
 * - Política de sesiones (stateless)
 * 
 * CORS: Configurado en CorsConfig.java (separado por SRP)
  */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configuración de la cadena de filtros de seguridad
     * 
     * - /api/v1/auth/registro (público)
     * - /api/v1/auth/login (público)
     * - Resto de endpoints protegidos con JWT
     * - CORS habilitado (configuración en CorsConfig.java)
     * 
     * @param http HttpSecurity builder
     * @return SecurityFilterChain configurado
     * @throws Exception si hay error en configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CORS (configuración viene de CorsConfig.java)
            // Spring inyecta automáticamente el bean corsConfigurationSource()
            .cors(cors -> {})  // Usa configuración del @Bean corsConfigurationSource
            
            // Deshabilitar CSRF (no necesario para API REST con JWT?)
            .csrf(csrf -> csrf.disable())
            
            // Configuración de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos 
                .requestMatchers("/api/v1/auth/registro").permitAll()
                .requestMatchers("/api/v1/auth/login").permitAll()
                
                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // Actuator
                .requestMatchers("/actuator/**").permitAll()
                
                // Resto de endpoints requieren autenticación (JWT)
                .anyRequest().authenticated()
            )
            
            // OAuth2 Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            
            // Política de sesión: Stateless (sin sesiones HTTP, solo JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Encoder de contraseñas (BCrypt)
     * 
     * Usado para hashear contraseñas en registro y para validar contraseñas en login
     * 
     * @return PasswordEncoder con algoritmo BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
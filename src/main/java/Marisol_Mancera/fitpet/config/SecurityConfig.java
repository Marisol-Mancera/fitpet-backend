package Marisol_Mancera.fitpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Seguridad en modo JWT stateless.
 * - Sin sesiones de servidor (STATELESS).
 * - CSRF desactivado para API REST.
 * - CORS habilitado para permitir comunicación con frontend.
 * - H2 console permitida y con frames sameOrigin.
 * - Endpoints públicos: /auth/registro, /auth/token, /auth/login, H2, Swagger.
 * - Resto autenticado mediante Bearer JWT.
 * 
 * Modificado en HU3 para añadir configuración CORS.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                // CORS habilitado
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // API stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF desactivado (no necesario en API REST stateless con JWT)
                .csrf(csrf -> csrf.disable())
                // H2 console + swagger públicos
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Consola H2 y documentación Swagger públicas (solo dev/test)
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Auth públicas (registro, login, token)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/token").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        // Cuando se emitan tokens con scopes, se puede afinar por scope:
                        // .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAuthority("SCOPE_USER")
                        // .requestMatchers("/api/v1/admin/**").hasAuthority("SCOPE_ADMIN")

                        // Resto de endpoints requieren autenticación
                        .anyRequest().authenticated())
                // Resource Server JWT (validación de tokens HS512)
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Convierte el claim "scope" en authorities con prefijo SCOPE_.
     * Ejemplo: scope: "USER ADMIN" -> authorities: SCOPE_USER, SCOPE_ADMIN
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var granted = new JwtGrantedAuthoritiesConverter();
        granted.setAuthoritiesClaimName("scope");
        granted.setAuthorityPrefix("SCOPE_");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(granted);
        return converter;
    }

}
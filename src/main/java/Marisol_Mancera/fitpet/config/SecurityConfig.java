package Marisol_Mancera.fitpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad en modo JWT stateless.
 * - Sin sesiones de servidor (STATELESS).
 * - CSRF desactivado para API REST.
 * - H2 console permitida y con frames sameOrigin.
 * - Endpoints públicos: /auth/registro, /auth/token, H2, Swagger.
 * - Resto autenticado mediante Bearer JWT.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API stateless
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            

            // H2 console + swagger públicos
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Auth públicas
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/token").permitAll()

                // cuando emita tokens con scopes, puedo afinar por scope:
                .requestMatchers("/api/v1/admin/**").hasAuthority("SCOPE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAuthority("SCOPE_USER")

                .anyRequest().authenticated()
            )

            // Resource Server JWT
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Convierte el claim "scope" en authorities con prefijo SCOPE_.
     * Ej.: scope: "USER ADMIN" -> authorities: SCOPE_USER, SCOPE_ADMIN
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
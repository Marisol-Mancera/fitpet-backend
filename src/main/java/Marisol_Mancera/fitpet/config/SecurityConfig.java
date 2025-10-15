package Marisol_Mancera.fitpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Security configuration for FitPet.
 * - Keeps CSRF enabled by default.
 * - Ignores CSRF only for H2 console endpoints (/h2-console/**).
 * - Allows frames from same origin so H2 console iframe can render.
 * - Exposes /auth/** as public (registration/login) per API contract.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/registro").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated()
            )

            // CSRF: keep ON, but ignore H2 console
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )

            // Allow frames for H2 console (same origin)
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // Basic defaults for now (we’ll move to JWT later)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
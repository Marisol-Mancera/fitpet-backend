package Marisol_Mancera.fitpet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Reloj del sistema para producción (UTC).
 * Permite inyectar Clock en componentes (p.ej. GlobalExceptionHandler)
 * y sustituirlo por Clock.fixed en tests.
 */
@Configuration
public class TimeConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}


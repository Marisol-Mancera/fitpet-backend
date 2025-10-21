package Marisol_Mancera.fitpet.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Manejo global de errores HTTP.
 * - Mapea ConflictException a 409.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Problem> handleConflict(ConflictException ex) {
        var body = new Problem("CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

     @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<Problem> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        // Mensaje controlado (no exponer detalles de seguridad)
        var body = new Problem("UNAUTHORIZED", "Invalid credentials");
        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).body(body);
    }

    /** Estructura mínima y estable de error para la API. */
    public record Problem(String code, String message) {
    }

}

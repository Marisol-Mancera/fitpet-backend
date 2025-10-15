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

    /** Estructura mínima y estable de error para la API. */
    public record Problem(String code, String message) {
    }
}

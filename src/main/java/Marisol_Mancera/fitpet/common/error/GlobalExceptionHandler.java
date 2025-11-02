package Marisol_Mancera.fitpet.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manejo global de errores HTTP. - Mapea ConflictException a 409.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Problem> handleConflict(ConflictException ex) {
        var body = new Problem("CONFLICT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Problem> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        // Mensaje controlado (no exponer detalles de seguridad)
        var body = new Problem("UNAUTHORIZED", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Estructura mínima y estable de error para la API.
     */
    public record Problem(String code, String message) {

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex) {

        var binding = ex.getBindingResult();
        var fieldErrors = binding.getFieldErrors();

        //1) si hay violaciones de @Pattern (espacios), priorizarlas
        String message = fieldErrors.stream()
                .filter(fe -> "Pattern".equals(fe.getCode())) // prioriza reglas de 'sin espacios'
                .map(fe -> fe.getDefaultMessage()) //fe --> abreviatura de FieldError
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                // 2) si no hay Pattern, usa el primer mensaje disponible (Email, NotBlank, etc.)
                .orElseGet(() -> fieldErrors.stream()
                .map(fe -> fe.getDefaultMessage())
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                .orElse("Validation failed"));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new Problem("BAD_REQUEST", message));
    }

    @ExceptionHandler(ResponseStatusException.class) 
public ResponseEntity<Problem> handleResponseStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == HttpStatus.NOT_FOUND) {
        String reason = ex.getReason();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new Problem(
                        "NOT_FOUND",
                        (reason != null && !reason.isBlank()) ? reason : "Pet not found"
                ));
    }
    throw ex;
}
}

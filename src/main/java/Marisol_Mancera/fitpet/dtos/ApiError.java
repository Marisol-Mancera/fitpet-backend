package Marisol_Mancera.fitpet.dtos;

/**
 * Representa un error de API con estructura uniforme.
 * Se usa en respuestas 4xx/5xx desde el GlobalExceptionHandler.
 * Nota: 'timestamp' en ISO-8601 (string) para evitar problemas de zona horaria en clientes.
 */
public record ApiError(
        int status,
        String error,
        String message,
        String path,
        String timestamp
) {}


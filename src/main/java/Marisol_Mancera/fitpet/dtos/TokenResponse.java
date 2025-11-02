package Marisol_Mancera.fitpet.dtos;

/**
 * DTO de salida del endpoint de autenticación (login).
 * Contiene el JWT emitido y metadatos básicos.
 */
public record TokenResponse(
        String tokenType,
        long expiresIn,
        String accessToken
) {}


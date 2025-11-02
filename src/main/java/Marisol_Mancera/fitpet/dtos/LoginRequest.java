package Marisol_Mancera.fitpet.dtos;


import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de entrada para login.
 * Valida formato de correo y presencia de password.
 */
public record LoginRequest(
        // @Email: Comprueba formato básico
        @Email(message = "email must be valid")
        @NotBlank(message = "email is required")
        // El regex ^\S+$ significa: empieza (^) y termina ($) con uno o más caracteres que NO son espacios (\S+).
        @Pattern(regexp = "^\\S+$", message = "Email must not contain spaces")
        String email,

        @NotBlank(message = "password is required")
        String password
) {
    /** Normaliza el email para búsquedas/almacenamiento consistentes. */
    public String normalizedEmail() {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}


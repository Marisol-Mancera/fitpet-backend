package Marisol_Mancera.fitpet.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Register payload.
 * Rules:
 *  - Non-empty valid email.
 *  - Password length >= 8, at least one digit and one symbol.
 */
public record RegisterRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = ".*\\d.*", message = "La contraseña debe contener al menos un número")
        @Pattern(regexp = ".*[^\\w\\s].*", message = "La contraseña debe contener al menos un símbolo")
        String password
) {}

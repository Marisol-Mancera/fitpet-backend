package Marisol_Mancera.fitpet.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import java.time.Instant;
import java.util.Set;

/**
 * Servicio de registro de usuarios.
 * Reglas:
 * - Si username (email) existe -> ConflictException (409).
 * - Guardar password como hash BCrypt.
 * - Devolver RegisterResponse con datos de alta.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        String username = request.email();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El correo ya está registrado");
        }

        String hash = passwordEncoder.encode(request.password());

        UserEntity toSave = UserEntity.builder()
                .username(username)
                .password(hash)
                .roles((Set.of())) // sin roles por ahora; se asignarán en pasos posteriores
                .build();

        UserEntity saved = userRepository.save(toSave);

        return new RegisterResponse(
                saved.getId().toString(),
                saved.getUsername(),
                Instant.now() // si prefieres, puedes usar una columna created_at luego
        );
    }
}

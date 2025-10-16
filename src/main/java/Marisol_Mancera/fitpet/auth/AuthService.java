package Marisol_Mancera.fitpet.auth;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import java.time.Instant;
import java.util.Set;

/**
 * Servicio de registro de usuarios.
 * - Verifica duplicados por username (email).
 * - Hashea la contraseña con BCrypt.
 * - Asigna ROLE_USER buscando por nombre (evita IDs mágicos).
 */
@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterResponse register(RegisterRequest request) {
        String username = request.email();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("El correo ya está registrado");
        }

        String hash = passwordEncoder.encode(request.password());

        RoleEntity roleUser = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role por defecto no configurado: " + DEFAULT_ROLE));

        UserEntity toSave = UserEntity.builder()
                .username(username)
                .password(hash)
                .roles(Set.of(roleUser))
                .build();

        UserEntity saved = userRepository.save(toSave);

        return new RegisterResponse(
                saved.getId().toString(),
                saved.getUsername(),
                Instant.now()
        );
    }
}
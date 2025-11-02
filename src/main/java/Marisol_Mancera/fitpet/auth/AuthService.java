package Marisol_Mancera.fitpet.auth;


import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.LoginRequest;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

/**
 * Servicio de registro de usuarios.
 * - Verifica duplicados por username (email).
 * - Hashea la contraseña con BCrypt.
 * - Asigna ROLE_USER buscando por nombre (evita IDs mágicos).
 * - Lanza ConflictException (409) si el email ya está registrado.
 * - Método adicional para autenticar credenciales (login).
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

    public AuthDTOResponse register(RegisterRequest request) {
        String username = request.email();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("The email is alredy registerd");
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

        return new AuthDTOResponse(
                saved.getId().toString(),
                saved.getUsername(),
                Instant.now()
        );
    }

    // autenticar credenciales; lanza 401 (BadCredentialsException) si no coincide
    public AuthDTOResponse authenticate(LoginRequest request) {
        final String username = request.normalizedEmail(); // normaliza email
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid credentials")); // ADD: 401

        if (!passwordEncoder.matches(request.password(), user.getPassword())) { //compara password con case sensitivity
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"); // ADD: 401
        }

        //respuesta mínima (token vendrá en otro micro-paso). Mantengo estructura usada en register para no romper DTO.
        return new AuthDTOResponse(
                user.getId().toString(),
                user.getUsername(),
                Instant.now()
        );
    }
}
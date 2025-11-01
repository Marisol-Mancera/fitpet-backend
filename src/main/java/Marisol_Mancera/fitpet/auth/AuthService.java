package Marisol_Mancera.fitpet.auth;

import java.time.Instant;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

/**
 * 
 * REFACTOR:
 * - ELIMINADO: método authenticate() (lógica movida a JwtTokenService)
 * - MANTIENE: método register() (usado por AuthController)
 * 
 * Responsabilidades:
 * - Verifica duplicados por username (email)
 * - Hashea la contraseña con BCrypt
 * - Asigna ROLE_USER por defecto (busca por nombre, evita IDs mágicos)
 * - Lanza ConflictException (409) si el email ya está registrado
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

    /**
     * HU1: Registra un nuevo usuario en el sistema.
     * 
     * Proceso:
     * 1. Verifica que el email (username) no esté duplicado
     * 2. Codifica la contraseña con BCrypt (passwordEncoder)
     * 3. Asigna ROLE_USER por defecto (busca en BD por nombre)
     * 4. Persiste el usuario en la BD
     * 5. Devuelve AuthDTOResponse con datos del usuario registrado
     * 
     * @param request RegisterRequest validado { email, password }
     * @return AuthDTOResponse con id, email y timestamp de creación
     * @throws ConflictException (409) si el email ya está registrado
     * @throws IllegalStateException si ROLE_USER no existe en BD
     */
    public AuthDTOResponse register(RegisterRequest request) {
        // 1. Obtener email del request (ya viene validado por @Valid en controlador)
        String username = request.email();

        // 2. Verificar si el email ya está registrado (evitar duplicados)
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("The email is already registered");
        }

        // 3. Codificar contraseña con BCrypt (nunca guardar en claro)
        String hash = passwordEncoder.encode(request.password());

        // 4. Obtener ROLE_USER de la BD (debe existir, creado por RoleSeeder)
        RoleEntity roleUser = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role por defecto no configurado: " + DEFAULT_ROLE));

        // 5. Crear entidad de usuario con datos validados
        UserEntity toSave = UserEntity.builder()
                .username(username)
                .password(hash)              // password codificada
                .roles(Set.of(roleUser))     // ROLE_USER asignado
                .build();

        // 6. Persistir en BD
        UserEntity saved = userRepository.save(toSave);

        // 7. Devolver DTO de respuesta (sin JWT, usuario debe hacer login después)
        return new AuthDTOResponse(
                saved.getId().toString(),    // id del usuario generado por BD
                saved.getUsername(),         // email normalizado
                Instant.now()                // timestamp de creación
        );
    }

    // ========================================
    // MÉTODO ELIMINADO (deprecado)
    // ========================================
    
    /**
     * ❌ ELIMINADO: authenticate(LoginRequest)
     * 
     * ANTES (código antiguo):
     * 
     * public AuthDTOResponse authenticate(LoginRequest request) {
     *     final String username = request.normalizedEmail();
     *     var user = userRepository.findByUsername(username)
     *             .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
     * 
     *     if (!passwordEncoder.matches(request.password(), user.getPassword())) {
     *         throw new BadCredentialsException("Invalid credentials");
     *     }
     * 
     *     return new AuthDTOResponse(
     *             user.getId().toString(),
     *             user.getUsername(),
     *             Instant.now()
     *     );
     * }
     * 
     * RAZÓN DE ELIMINACIÓN:
     * - Devolvía AuthDTOResponse sin JWT (inútil para autenticación)
     * - Duplicaba lógica de JwtTokenService.loginAndGenerateToken()
     * - Solo se usaba en el endpoint /login viejo (que fue eliminado)
     * - JwtTokenService ya valida credenciales Y genera JWT en un solo método
     * 
     * MIGRACIÓN:
     * - Usar JwtTokenService.loginAndGenerateToken() directamente
     * - Devuelve String (JWT) en lugar de AuthDTOResponse
     * - Mismo proceso de autenticación, mejor resultado
     */
}
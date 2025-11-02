package Marisol_Mancera.fitpet.auth;

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.LoginRequest;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.TokenResponse;
import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.security.JwtTokenService;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador de autenticación para emitir JWT (HS512).
 * Rutas:
 *  - POST /api/v1/auth/token (deprecado, usar /login)
 *  - POST /api/v1/auth/login (recomendado)
 *  - POST /api/v1/auth/registro
 * Seguridad: endpoints públicos (permitAll) configurados en SecurityConfig.
 */
@RestController
@RequestMapping(path = "/${api-endpoint}/v1/auth")
@RequiredArgsConstructor
public class AuthTokenController {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    /**
     * HU2: Login con JWT
     * Endpoint: POST /api/v1/auth/login
     * 
     * Emite un JWT si las credenciales son válidas.
     * - Normaliza el email (trim + lowercase) para evitar fallos de coincidencia.
     * - Responde 201 Created con { tokenType, expiresIn, accessToken }.
     * - Si las credenciales son inválidas, lanza BadCredentialsException (mapeada a 401).
     * 
     * @param request LoginRequest con email y password
     * @return TokenResponse con JWT
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        // Normalizar email
        String normalizedEmail = request.normalizedEmail();
        
        // Generar JWT usando JwtTokenService
        String jwt = jwtTokenService.loginAndGenerateToken(normalizedEmail, request.password());
        
        // TTL coordinado con JwtTokenService (2 horas después del cambio)
        long expiresInSeconds = Duration.ofHours(2).toSeconds();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TokenResponse("Bearer", expiresInSeconds, jwt));
    }

    /**
     * Endpoint /token (DEPRECADO - usar /login en su lugar)
     * Mantenido por compatibilidad con código legacy.
     * 
     * Emite un JWT si las credenciales son válidas.
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody LoginRequest request) {
        // Normalizamos el email recibido para búsquedas consistentes en BD
        String normalizedEmail = request.normalizedEmail();

        // Delegamos en el servicio de dominio la autenticación y emisión del JWT
        String jwt = jwtTokenService.loginAndGenerateToken(normalizedEmail, request.password());

        // TTL coordinado con el servicio (2 horas después del cambio)
        long expiresInSeconds = Duration.ofHours(2).toSeconds();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new TokenResponse("Bearer", expiresInSeconds, jwt));
    }

    /**
     * HU1: Registro de usuario
     * Endpoint: POST /api/v1/auth/registro
     * 
     * Crea un nuevo usuario con ROLE_USER.
     * - Normaliza el email (trim + lowercase).
     * - Hashea la contraseña con BCrypt.
     * - Responde 201 Created con { id, email, createdAt }.
     * - Si el email ya existe, lanza ConflictException (mapeada a 409).
     * 
     * @param request RegisterRequest con email y password
     * @return AuthDTOResponse con datos del usuario creado
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthDTOResponse> register(@RequestBody @Valid RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(email)) {
            throw new ConflictException("Email already registered");
        }

        RoleEntity roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(roleUser))
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthDTOResponse("Registered", email, null));
    }
}
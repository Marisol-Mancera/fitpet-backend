package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

import java.time.Duration;
import java.util.Locale;
import java.util.Set;

//  Controlador de autenticación para emitir JWT (HS512).
//  Ruta: POST /api/auth/token  (api-endpoint = 'api')
//  Seguridad: endpoint público (permitAll) configurado en SecurityConfig.

@RestController
@RequestMapping(path = "/${api-endpoint}/v1/auth")
public class AuthTokenController {

    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthTokenController(JwtTokenService jwtTokenService,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;}
    /**
     * Emite un JWT si las credenciales son válidas.
     * - Normaliza el email (trim + lowercase) para evitar fallos de coincidencia.
     * - Responde 201 con { tokenType, expiresIn, accessToken }.
     * - Si las credenciales son inválidas, el servicio lanza
     * BadCredentialsException (mapeada a 401 por el handler global).
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody LoginRequest request) {
        // Normalizamos el email recibido para búsquedas consistentes en BD
        String normalizedEmail = request.normalizedEmail();

        // Delegamos en el servicio de dominio la autenticación y emisión del JWT
        String jwt = jwtTokenService.loginAndGenerateToken(normalizedEmail, request.password());

        // TTL coordinado con el servicio (15 minutos por defecto)
        long expiresInSeconds = Duration.ofMinutes(15).toSeconds();

        return ResponseEntity
                .status(201)
                .body(new TokenResponse("Bearer", expiresInSeconds, jwt));
    }

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
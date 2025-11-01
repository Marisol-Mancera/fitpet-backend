package Marisol_Mancera.fitpet.auth;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Marisol_Mancera.fitpet.dtos.LoginRequest;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.TokenResponse;
import Marisol_Mancera.fitpet.security.JwtTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador unificado de autenticación.
 * 
 * REFACTOR CONSOLIDADO:
 * - Antes: AuthController (solo /registro) + AuthTokenController (/token, /login)
 * - Ahora: Un solo AuthController con todos los endpoints
 * - Delegación completa a servicios (AuthService, JwtTokenService)
 * - Validación con @Valid (Bean Validation en capa web)
 * 
 * Endpoints:
 * - POST /api/v1/auth/registro → HU1: Registro de usuario (delegado a AuthService)
 * - POST /api/v1/auth/login    → HU2: Login con JWT (delegado a JwtTokenService)
 * 
 * ELIMINADO:
 * - POST /api/v1/auth/token (deprecado, funcionalidad movida a /login)
 * 
 * Seguridad: endpoints públicos (permitAll) configurados en SecurityConfig
 */
@RestController
@RequestMapping(path = "${api-endpoint:/api/v1}/auth")
@RequiredArgsConstructor // Lombok genera constructor con 'final' fields
public class AuthController {

    private final AuthService authService;           // Lógica de registro
    private final JwtTokenService jwtTokenService;   // Lógica de autenticación + JWT

    /**
     * Registro de nuevo usuario
     * 
     * Proceso (delegado a AuthService):
     * 1. Normaliza email (trim + lowercase)
     * 2. Verifica que email no esté registrado (409 Conflict si existe)
     * 3. Asigna ROLE_USER por defecto
     * 4. Codifica password con BCrypt
     * 5. Persiste usuario en BD
     * 6. Devuelve AuthDTOResponse (sin JWT, usuario debe hacer login después)
     * 
     * Validaciones (@Valid):
     * - @Email: formato email válido
     * - @NotBlank: campos requeridos
     * - @Pattern: contraseña segura (8+ chars, número, símbolo)
     * 
     * Respuestas:
     * - 201 Created: Usuario registrado exitosamente
     * - 400 Bad Request: Validación fallida (contraseña débil, formato email, etc.)
     * - 409 Conflict: Email ya registrado
     * 
     * @param request RegisterRequest validado { email, password }
     * @return ResponseEntity<AuthDTOResponse> con usuario registrado
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthDTOResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Delegación completa al servicio (SRP: controlador NO tiene lógica de negocio)
        AuthDTOResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login de usuario (endpoint unificado, reemplaza a /token)
     * 
     * Proceso (delegado a JwtTokenService):
     * 1. Recibe LoginRequest con validaciones (@Email, @Pattern sin espacios)
     * 2. Normaliza el email (trim + lowercase) para búsquedas consistentes
     * 3. Valida credenciales y genera JWT mediante JwtTokenService
     * 4. Devuelve TokenResponse { tokenType, expiresIn, accessToken }
     * 
     * Validaciones (@Valid):
     * - @Email: formato email válido
     * - @NotBlank: campos requeridos
     * - @Pattern(^\S+$): email sin espacios
     * 
     * Respuestas:
     * - 201 Created: Login exitoso con JWT
     * - 400 Bad Request: Email con espacios o formato inválido
     * - 401 Unauthorized: Credenciales inválidas o password case mismatch
     *                     (lanzado por JwtTokenService → BadCredentialsException)
     * 
     * Escenarios:
     * Login exitoso con credenciales válidas sin espacios
     * Error si email contiene espacios (validación @Pattern)
     * Error si password contiene espacios (frontend lo previene)
     * Error si credenciales inválidas (401)
     * Error si password case mismatch (401)
     * 
     * NOTA: Este endpoint reemplaza al antiguo /token con semántica más clara
     * 
     * @param request LoginRequest validado { email, password }
     * @return ResponseEntity<TokenResponse> con JWT y expiración
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        // 1. Normalizar email para búsquedas consistentes en BD
        //    (trim elimina espacios al inicio/final, toLowerCase evita case mismatch en email)
        String normalizedEmail = request.normalizedEmail();

        // 2. Delegar autenticación y generación de JWT al servicio de dominio
        //    - Valida credenciales con passwordEncoder.matches()
        //    - Si falla, lanza BadCredentialsException → GlobalExceptionHandler mapea a 401
        //    - Si éxito, genera JWT firmado con HS512
        String jwt = jwtTokenService.loginAndGenerateToken(normalizedEmail, request.password());

        // 3. Calcular expiración del token (coordinado con JwtTokenService)
        //    Valor por defecto: 15 minutos
        long expiresInSeconds = Duration.ofMinutes(15).toSeconds();

        // 4. Responder 201 Created con TokenResponse
        //    Frontend guardará accessToken en localStorage
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created (recurso JWT creado)
                .body(new TokenResponse(
                    "Bearer",           // tokenType para header Authorization
                    expiresInSeconds,   // 900 segundos (15 min)
                    jwt                 // accessToken JWT firmado
                ));
    }

    // ========================================
    // ENDPOINT ELIMINADO (deprecado)
    // ========================================
    
    /**
     * ❌ ELIMINADO: /token (deprecado)
     * 
     * ANTES (en AuthTokenController.java):
     * 
     * @PostMapping("/token")
     * public ResponseEntity<TokenResponse> token(@RequestBody LoginRequest request) {
     *     String normalizedEmail = request.normalizedEmail();
     *     String jwt = jwtTokenService.loginAndGenerateToken(normalizedEmail, request.password());
     *     long expiresInSeconds = Duration.ofMinutes(15).toSeconds();
     *     return ResponseEntity.status(201).body(new TokenResponse("Bearer", expiresInSeconds, jwt));
     * }
     * 
     * RAZÓN DE ELIMINACIÓN:
     * - Duplicaba funcionalidad con /login
     * - /login es más semántico para autenticación
     * - Evita confusión al tener dos endpoints que hacen lo mismo
     * - Todo consolidado en AuthController (antes tenía 2 controladores)
     * 
     * MIGRACIÓN:
     * - Frontend debe cambiar de `/token` a `/login`
     * - Ambos endpoints devuelven TokenResponse (misma estructura)
     * - No hay cambios en la lógica, solo en la URL
     */
}
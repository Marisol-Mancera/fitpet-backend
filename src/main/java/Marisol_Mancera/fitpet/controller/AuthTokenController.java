package Marisol_Mancera.fitpet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Marisol_Mancera.fitpet.dtos.LoginRequest;
import Marisol_Mancera.fitpet.dtos.TokenResponse;
import Marisol_Mancera.fitpet.security.JwtTokenService;

import java.time.Duration;


//  Controlador de autenticación para emitir JWT (HS512).
//  Ruta: POST /api/auth/token  (api-endpoint = 'api')
//  Seguridad: endpoint público (permitAll) configurado en SecurityConfig.

@RestController
@RequestMapping(path = "/${api-endpoint}/v1/auth")
public class AuthTokenController {

    private final JwtTokenService jwtTokenService;

    public AuthTokenController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Emite un JWT si las credenciales son válidas.
     * - Normaliza el email (trim + lowercase) para evitar fallos de coincidencia.
     * - Responde 201 con { tokenType, expiresIn, accessToken }.
     * - Si las credenciales son inválidas, el servicio lanza BadCredentialsException (mapeada a 401 por el handler global).
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
}
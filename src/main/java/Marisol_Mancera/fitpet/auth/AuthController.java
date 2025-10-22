package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import jakarta.validation.Valid;


/**
 * Controlador de autenticación/registro.
 * - Delegación completa al AuthService (sin estado en el controlador).
 * - Valida el payload con @Valid (Bean Validation en capa web).
 * - Mantiene el contrato: 201 con RegisterResponse; 400 si payload inválido; 409 si email duplicado.
 */
@RestController
@RequestMapping(path = "${api-endpoint:/api/v1}/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
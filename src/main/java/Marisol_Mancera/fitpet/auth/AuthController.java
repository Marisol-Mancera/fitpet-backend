package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Marisol_Mancera.fitpet.common.error.ConflictException;
import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping(path = "${api-endpoint:/api/v1}/auth")
public class AuthController {

    // Registro concurrente mínimo para detectar duplicados en esta iteración (solo para pasar el test 409).
    private static final Set<String> REGISTERED_EMAILS = ConcurrentHashMap.newKeySet();

    @PostMapping("/registro")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email();

        // Si el email ya fue registrado, devolvemos 409 (Conflict)
        if (REGISTERED_EMAILS.contains(email)) {
            throw new ConflictException("El correo ya está registrado");
        }

        // "Registrar" en memoria (siguiente paso: mover a Service + JPA Repository)
        REGISTERED_EMAILS.add(email);

        var response = new RegisterResponse(
                UUID.randomUUID().toString(),
                email,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

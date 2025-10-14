package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint mínimo de registro para validar el contrato HTTP y el wiring con seguridad.
 * Luego lo refactorizaremos a DTOs + servicio real, manteniendo los tests.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", "fake-id-001");
        response.put("email", body.get("email"));
        response.put("createdAt", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
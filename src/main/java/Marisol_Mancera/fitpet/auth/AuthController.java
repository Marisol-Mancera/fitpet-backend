package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping(path = "${api-endpoint}/auth") 
public class AuthController {

    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        // stub mientras implementamos la lógica real
        Map<String, Object> body = new HashMap<>();
        body.put("id", "fake-id-001");
        body.put("email", request.email());
        body.put("createdAt", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
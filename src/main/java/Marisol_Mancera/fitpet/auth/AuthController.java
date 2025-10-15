package Marisol_Mancera.fitpet.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.UUID;


@RestController
@RequestMapping(path = "${api-endpoint}/auth") 
public class AuthController {

    @PostMapping("/registro")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        // stub mientras implementamos la lógica real
        var response = new RegisterResponse(
                UUID.randomUUID().toString(), // temporary id
                request.email(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
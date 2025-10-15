package Marisol_Mancera.fitpet.dtos;

import java.time.Instant;

//outband para registro satisactorio
public record RegisterResponse(
        String id,
        String email,
        Instant createdAt
) {}


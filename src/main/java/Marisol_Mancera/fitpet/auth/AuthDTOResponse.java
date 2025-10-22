package Marisol_Mancera.fitpet.auth;

import java.time.Instant;

//outband para registro satisactorio
public record AuthDTOResponse(
        String id,
        String email,
        Instant createdAt
) {}


package Marisol_Mancera.fitpet.dtos;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ApiErrorTest {

    @Test
    @DisplayName("Debe construir ApiError con todos los campos accesibles")
    void should_build_api_error_with_all_fields() {
        var dto = new ApiError(401, "Unauthorized", "Invalid credentials",
                "/api/v1/auth/token", "2025-10-21T10:00:00Z");

        assertThat(dto.status(), is(401));
        assertThat(dto.error(), is("Unauthorized"));
        assertThat(dto.message(), is("Invalid credentials"));
        assertThat(dto.path(), is("/api/v1/auth/token"));
        assertThat(dto.timestamp(), is("2025-10-21T10:00:00Z"));
    }
}


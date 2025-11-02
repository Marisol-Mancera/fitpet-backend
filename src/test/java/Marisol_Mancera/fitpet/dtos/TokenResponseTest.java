package Marisol_Mancera.fitpet.dtos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TokenResponseTest {

    @Test
    @DisplayName("Debe crear una respuesta JWT con tipo Bearer y expiración positiva")
    void should_create_valid_token_response() {
        var response = new TokenResponse("Bearer", 900, "mock.jwt.token.value");

        assertThat(response.tokenType(), is("Bearer"));
        assertThat(response.expiresIn(), greaterThan(0L));
        assertThat(response.accessToken(), is("mock.jwt.token.value"));
    }
}


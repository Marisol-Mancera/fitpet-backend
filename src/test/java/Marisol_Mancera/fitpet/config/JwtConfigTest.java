package Marisol_Mancera.fitpet.config;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class JwtConfigTest {

    @Autowired
    JwtEncoder encoder;

    @Autowired
    JwtDecoder decoder;

    @Test
    @DisplayName("JwtEncoder/JwtDecoder deben firmar y validar un token con claim 'scope'")
    void should_encode_and_decode_jwt_with_scope_claim() {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("owner@example.com")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.MINUTES))
                .claims(map -> map.putAll(Map.of("scope", "USER ADMIN")))
                .build();

        JwsHeader headers = JwsHeader.with(() -> "HS512").build();

        Jwt encoded = encoder.encode(JwtEncoderParameters.from(headers, claims));
        assertThat(encoded.getTokenValue(), not(emptyOrNullString()));

        Jwt decoded = decoder.decode(encoded.getTokenValue());
        assertThat(decoded.getSubject(), is("owner@example.com"));
        assertThat(decoded.getClaims().get("scope"), is("USER ADMIN"));
        assertThat(decoded.getExpiresAt(), notNullValue());
    }
}


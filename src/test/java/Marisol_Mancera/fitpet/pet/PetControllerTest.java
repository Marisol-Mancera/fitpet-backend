package Marisol_Mancera.fitpet.pet;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class PetControllerTest {

    @Autowired JwtEncoder 
    jwtEncoder;
    @Autowired UserRepository 
    userRepository;
    @Autowired MockMvc 
    mockMvc;

    private String bearerFor(String username) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .claim("scope", "USER")
                .build();
        var headers = JwsHeader.with(MacAlgorithm.HS512).build();
        var token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
        return "Bearer " + token;
    }

    @Test
    @DisplayName("201 crear mascota: devuelve la ubicación y PetDTOResponse para el propietario autenticado (Bearer JWT)")
    void should_create_pet_and_return_201_with_location_and_body() throws Exception {
        // Arrange
        var owner = UserEntity.builder()
                .username("pajaritopio@example.com")
                .password("any")
                .roles(java.util.Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String petJson = """
        {
          "name": "Pony",
          "species": "Dog",
          "breed": "Beagle",
          "sex": "Female",
          "birthDate": "%s",
          "weightKg": 12.4
        }
        """.formatted(java.time.LocalDate.now().minusYears(3));

        var result = mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(petJson))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/pets/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ownerId").value(owner.getId()))
                .andExpect(jsonPath("$.name").value("Pony"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.breed").value("Beagle"))
                .andExpect(jsonPath("$.sex").value("Female"))
                .andExpect(jsonPath("$.weightKg").value(12.4))
                .andReturn();

        assertThat(result.getResponse().getHeader("Location")).isNotBlank();
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando el nombre está en blanco")
    void should_return_400_when_name_is_blank() throws Exception {
        var owner = UserEntity.builder()
                .username("pajaritopi0@example.com")
                .password("any")
                .roles(java.util.Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
        {
          "name": "   ",
          "species": "Dog",
          "breed": "Beagle",
          "sex": "Female",
          "birthDate": "%s",
          "weightKg": 12.4
        }
        """.formatted(java.time.LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must not be blank")));
    }
}

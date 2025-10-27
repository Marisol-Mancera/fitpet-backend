package Marisol_Mancera.fitpet.pet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    JwtEncoder jwtEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MockMvc mockMvc;

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
        """.formatted(LocalDate.now().minusYears(3));

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
                .roles(Collections.emptySet())
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
        """.formatted(LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must not be blank")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando la fecha de nacimiento no es pasada")
    void should_return_400_when_birth_date_is_not_past() throws Exception {
        var owner = UserEntity.builder()
                .username("losgatosdekaren@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 12.4
    }
    """.formatted(LocalDate.now().plusDays(1)); // fecha futura → inválida

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must be a past")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando el peso no es positivo")
    void should_return_400_when_weight_is_not_positive() throws Exception {
        var owner = UserEntity.builder()
                .username("doggylove@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": -2.5
    }
    """.formatted(LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must be greater than 0")));
    }

    @Test
    @DisplayName("401 crear mascota: UNAUTHORIZED cuando no hay token Bearer")
    void should_return_401_when_no_bearer_token() throws Exception {
        String validJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 12.4
    }
    """.formatted(LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
                .andExpect(status().isUnauthorized())
                // El Resource Server expone este header con el esquema requerido
                .andExpect(header().string("WWW-Authenticate", containsString("Bearer")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando la fecha de nacimiento es nula")
    void should_return_400_when_birth_date_is_null() throws Exception {
        var owner = UserEntity.builder()
                .username("pajaritopio65@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        // birthDate: null (sin comillas) para que el parser lo trate como JSON null
        String invalidJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": null,
      "weightKg": 12.4
    }
    """;

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must not be null")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando el body está vacío")
    void should_return_400_when_body_is_empty() throws Exception {
        var owner = UserEntity.builder()
                .username("pollitopio@example.com")
                .password("any")
                .roles(java.util.Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando la especie está en blanco")
    void should_return_400_when_species_is_blank() throws Exception {
        var owner = UserEntity.builder()
                .username("vacaypollo@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
    {
      "name": "Pony",
      "species": "   ",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 12.4
    }
    """.formatted(LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", containsString("must not be blank")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando la raza está en blanco")
    void should_return_400_when_breed_is_blank() throws Exception {
        var owner = UserEntity.builder()
                .username("pajaritomeztizo@example.com")
                .password("any")
                .roles(java.util.Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "   ",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 12.4
    }
    """.formatted(java.time.LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("must not be blank")));
    }

    @Test
    @DisplayName("400 crear mascota: BAD_REQUEST cuando el sexo está en blanco")
    void should_return_400_when_sex_is_blank() throws Exception {
        var owner = UserEntity.builder()
                .username("pajaritopiohavenosex@example.com")
                .password("any")
                .roles(java.util.Collections.emptySet())
                .build();
        userRepository.save(owner);

        String bearer = bearerFor(owner.getUsername());

        String invalidJson = """
    {
      "name": "Pony",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "   ",
      "birthDate": "%s",
      "weightKg": 12.4
    }
    """.formatted(java.time.LocalDate.now().minusYears(3));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("must not be blank")));
    }

    @Test
    @DisplayName("200 listar mascotas: devuelve solo las del dueño autenticado")
    void should_return_only_authenticated_owner_pets() throws Exception {
        var ownerA = UserEntity.builder()
                .username("lakarenmola@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        var ownerB = UserEntity.builder()
                .username("elkarenmacho@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(ownerA);
        userRepository.save(ownerB);

        String bearerA = bearerFor(ownerA.getUsername());
        String bearerB = bearerFor(ownerB.getUsername());

        String petAJson = """
    {
      "name": "PonyA",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 10.0
    }
    """.formatted(LocalDate.now().minusYears(2));

        String petBJson = """
    {
      "name": "CattyB",
      "species": "Cat",
      "breed": "Siamese",
      "sex": "Male",
      "birthDate": "%s",
      "weightKg": 4.2
    }
    """.formatted(LocalDate.now().minusYears(1));

        // Creamos una mascota para cada usuario vía POST
        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerA)
                .content(petAJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearerB)
                .content(petBJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/pets")
                .header("Authorization", bearerA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("PonyA"))
                .andExpect(jsonPath("$[0].species").value("Dog"))
                .andExpect(jsonPath("$[0].breed").value("Beagle"))
                .andExpect(jsonPath("$[0].sex").value("Female"))
                .andExpect(jsonPath("$[0].ownerId").value(ownerA.getId()));
    }

    @Test
    @DisplayName("401 listar mascotas: UNAUTHORIZED cuando no hay token Bearer")
    void should_return_401_when_list_without_bearer_token() throws Exception {
        mockMvc.perform(get("/api/v1/pets"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Bearer")));
    }

    @Test
    @DisplayName("200 listar mascotas: devuelve array vacío cuando el dueño no tiene mascotas")
    void should_return_empty_list_when_owner_has_no_pets() throws Exception {
        //usuario sin mascotas
        var lonely = UserEntity.builder()
                .username("karensinmascota@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(lonely);

        String bearer = bearerFor(lonely.getUsername());

        mockMvc.perform(get("/api/v1/pets")
                .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("200 listar mascotas: devuelve array con campos mínimos")
    void should_return_list_with_minimal_schema_fields() throws Exception {
        //dueño con 1 mascota para validar el esquema
        var owner = UserEntity.builder()
                .username("lasmascotasdelkarenmacho@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);
        String bearer = bearerFor(owner.getUsername());

        String petJson = """
    {
      "name": "PonySchema",
      "species": "Dog",
      "breed": "Beagle",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 9.9
    }
    """.formatted(LocalDate.now().minusYears(2));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(petJson))
                .andExpect(status().isCreated());

        //GET debe devolver 1 elemento con los campos mínimos
        mockMvc.perform(get("/api/v1/pets")
                .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].ownerId").value(owner.getId()))
                .andExpect(jsonPath("$[0].name").value("PonySchema"))
                .andExpect(jsonPath("$[0].species").value("Dog"))
                .andExpect(jsonPath("$[0].breed").value("Beagle"))
                .andExpect(jsonPath("$[0].sex").value("Female"))
                .andExpect(jsonPath("$[0].birthDate").exists())
                .andExpect(jsonPath("$[0].weightKg").value(9.9));
    }

    @Test
    @DisplayName("200 listar mascotas: devuelve todas las mascotas del dueño (2 elementos)")
    void should_return_two_pets_for_owner() throws Exception {
        // dueño con 2 mascotas
        var owner = UserEntity.builder()
                .username("elkaren+2@example.com")
                .password("any")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(owner);
        String bearer = bearerFor(owner.getUsername());

        String pet1 = """
    {
      "name": "Luna",
      "species": "cat",
      "breed": "siames",
      "sex": "Female",
      "birthDate": "%s",
      "weightKg": 5.5
    }
    """.formatted(LocalDate.now().minusYears(4));

        String pet2 = """
    {
      "name": "Max",
      "species": "Dog",
      "breed": "Labrador",
      "sex": "Male",
      "birthDate": "%s",
      "weightKg": 28.0
    }
    """.formatted(LocalDate.now().minusYears(5));

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(pet1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", bearer)
                .content(pet2))
                .andExpect(status().isCreated());

        // GET debe traer las 2 mascotas del dueño
        mockMvc.perform(get("/api/v1/pets")
                .header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ownerId").value(owner.getId()))
                .andExpect(jsonPath("$[1].ownerId").value(owner.getId()));
    }

}

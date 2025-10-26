package Marisol_Mancera.fitpet.pet;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("201 create pet: returns Location and PetDTOResponse for authenticated owner")
    @WithMockUser(username = "pajaritopio@example.com", roles = {"USER"})
    void should_create_pet_and_return_201_with_location_and_body() throws Exception {

        // Creamos un usuario cuyo 'username' coincide con el principal autenticado 
        UserEntity owner = UserEntity.builder()
                .username("pajaritopio@example.com")
                .password("notimportant")
                .build();

        userRepository.save(owner);

        String json = """
            {
              "name": "Pony",
              "species": "Dog",
              "breed": "Beagle",
              "sex": "Female",
              "birthDate": "%s",
              "weightKg": 12.4
            }
            """.formatted(LocalDate.now().minusYears(3));

        var mvcResult = mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/v1/pets/\\d+")))
                .andExpect(jsonPath("$.id").isNumber()) // [+]
                .andExpect(jsonPath("$.ownerId").value(owner.getId())) //debe devolver el id del dueño autenticado
                .andExpect(jsonPath("$.name").value("Pony"))
                .andExpect(jsonPath("$.species").value("Dog"))
                .andExpect(jsonPath("$.breed").value("Beagle"))
                .andExpect(jsonPath("$.sex").value("Female"))
                .andExpect(jsonPath("$.weightKg").value(12.4))
                .andReturn(); // [+]

        String location = mvcResult.getResponse().getHeader("Location");
        assertThat(location).isNotBlank();
    }

    @Test
    @DisplayName("400 create pet: BAD_REQUEST when name is blank")
    @WithMockUser(username = "pajaritopio@example.com", roles = {"USER"})
    void should_return_400_when_name_is_blank() throws Exception {
        // Arrange
        var owner = UserEntity.builder()
                .username("pajaritopio@example.com")
                .password("encoded") // si tu entidad exige no-null en BD; si no hace falta, puedes quitarlo
                .build();
        userRepository.save(owner);

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

        // Act & Assert
        mockMvc.perform(post("/api/v1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("must not be blank"));
    }
}

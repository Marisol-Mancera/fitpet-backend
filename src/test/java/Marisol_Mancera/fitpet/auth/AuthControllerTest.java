package Marisol_Mancera.fitpet.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired 
    WebApplicationContext context;
    @Autowired 
    ObjectMapper mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()) // ✅ seguridad aplicada
                .build();
    }

     @Test
    @DisplayName("Registro de usuario devuelve 201 y contiene el correo en la respuesta")
    @WithMockUser // usuario simulado para evitar fallos de seguridad
    void Should_Register_user_and_return_201_and_include_email() throws Exception {
        var payload = Map.of("email", "pajaritopio@example.com", "password", "Str0ng!Pass");

        MockHttpServletResponse response = mockMvc.perform(
                post("/auth/registro")
                        .with(csrf()) // 👈 necesario para POST si CSRF está habilitado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        assertThat(response.getStatus(), is(201));
        assertThat(response.getContentAsString(), containsString("pajaritopio@example.com"));
    }
}
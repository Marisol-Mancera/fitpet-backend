package Marisol_Mancera.fitpet.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import org.springframework.beans.factory.annotation.Autowired;


@SpringBootTest
@TestPropertySource(properties = "api-endpoint=/api/v1") 
class AuthControllerTest {

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Debe devolver 201 al registrar usuario en /api/v1/auth/registro")
    @WithMockUser
    void should_return_201_when_registering_user_under_property_mapped_base_path() throws Exception {
        var payload = java.util.Map.of("email", "owner@example.com", "password", "Str0ng!Pass");

        mockMvc.perform(
                post("/api/v1/auth/registro") 
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(payload))
        ).andExpect(status().isCreated());
    }

    }

package Marisol_Mancera.fitpet.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

/**
 * Caso feliz: credenciales válidas -> 201 + JSON con accessToken Bearer.
 * Base path usa 'api-endpoint=api' => /api/auth/token
 */
@SpringBootTest
class AuthTokenControllerTest {

        @Autowired
        WebApplicationContext context;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        RoleRepository roleRepository;
        @Autowired
        PasswordEncoder passwordEncoder;

        MockMvc mockMvc;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(context)
                                .apply(springSecurity())
                                .build();

                // Asegurar ROLE_USER (idempotente)
                var roleUser = roleRepository.findByName("ROLE_USER")
                                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name("ROLE_USER").build()));

                // Usuario fijo para los tests (norma: pajaritopio@example.com) con credenciales
                // deterministas
                var email = "pajaritopio@example.com";
                var encoded = passwordEncoder.encode("Str0ng!Pass");

                userRepository.findByUsername(email).ifPresentOrElse(existing -> {
                        // Forzar password y rol por si vienen de otro test/contexto
                        existing.setPassword(encoded);
                        existing.setRoles(Set.of(roleUser));
                        userRepository.save(existing);
                }, () -> {
                        userRepository.save(UserEntity.builder()
                                        .username(email)
                                        .password(encoded)
                                        .roles(Set.of(roleUser))
                                        .build());
                });
        }

        @Test
        @DisplayName("201 cuando credenciales son válidas: devuelve Bearer accessToken")
        void should_return_201_and_bearer_token_on_valid_credentials() throws Exception {
                var body = Map.of(
                                "email", "pajaritopio@example.com",
                                "password", "Str0ng!Pass");

                var result = mockMvc.perform(post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(body)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                                .andExpect(jsonPath("$.expiresIn").exists())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn();

                assertThat(result.getResponse().getContentAsString(), not(emptyOrNullString()));
        }

        @Test
        @DisplayName("401 cuando password es inválida: devuelve JSON de error consistente")
        void should_return_401_when_password_is_invalid() throws Exception {
                var body = Map.of(
                                "email", "pajaritopio@example.com",
                                "password", "Wrong!Pass");

                mockMvc.perform(post("/api/v1/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(body)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        }

        @Test
        @DisplayName("400 cuando el payload de registro no cumple validaciones (sin símbolo)")
        void should_return_400_when_register_payload_is_invalid() throws Exception {
                var body = Map.of(
                                "email", "pajaritopio@example.com",
                                "password", "Abcdef12");

                mockMvc.perform(post("/api/v1/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(body)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 cuando el payload es inválido (sin símbolo en password) devuelve Problem")
        void should_return_400_problem_when_register_payload_is_invalid() throws Exception {
                var body = java.util.Map.of(
                                "email", "pajaritopio@example.com",
                                "password", "Abcdef12");

                mockMvc.perform(post("/api/v1/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(body)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                                .andExpect(jsonPath("$.message", not(emptyOrNullString())));
        }

        @Test
        @DisplayName("201 registro válido: devuelve AuthDTOResponse y persiste usuario con ROLE_USER")
        void should_return_201_and_persist_user_with_role_user() throws Exception {
                var email = "pajaritopi0@example.com";
                var body = Map.of(
                                "email", email,
                                "password", "Str0ng!Pass" // >=8, con dígito y símbolo
                );

                mockMvc.perform(post("/api/v1/auth/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(body)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value("Registered"))
                                .andExpect(jsonPath("$.email").value(email))
                                .andExpect(jsonPath("$.token").doesNotExist()); 
                                                

                // Verificaciones en BD
                var savedOpt = userRepository.findByUsername(email);
                assertThat("El usuario debe existir tras el registro", savedOpt.isPresent(), is(true));

                UserEntity saved = savedOpt.get();
                assertThat("La contraseña NO debe guardarse en claro",
                                saved.getPassword(), not(equalTo("Str0ng!Pass")));
                assertThat("El encoder debe validar la contraseña codificada",
                                passwordEncoder.matches("Str0ng!Pass", saved.getPassword()), is(true));

                var roles = saved.getRoles();
                assertThat("El usuario debe tener ROLE_USER", roles, is(not(empty())));
                boolean hasRoleUser = roles.stream().map(r -> r.getName()).anyMatch("ROLE_USER"::equals);
                assertThat(hasRoleUser, is(true));
        }

}

package Marisol_Mancera.fitpet.auth;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.equalTo;

import com.fasterxml.jackson.databind.ObjectMapper;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

/**
 * Caso feliz: credenciales válidas -> 201 + JSON con accessToken Bearer. Base
 * path usa 'api-endpoint=api' => /api/auth/token
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

        // Asegurar usuario de prueba (idempotente)
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

    @BeforeEach //garantiza base de datos limpia al iniciar cada test
    void cleanupDatabase() {
        userRepository.deleteAllInBatch(); //más rápido y evita cascadas parciales
    }

    /**
     ********************************************
     * Tests para el endpoint /api/v1/auth/token*
     * *******************************************
     *
     */
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

    /**
     ***********************************************
     * Tests para el endpoint /api/v1/auth/registro*
     * **********************************************
     *
     */
    @Test
    @DisplayName("400 cuando el payload de registro no cumple validaciones (sin símbolo)")
    void should_return_400_when_register_payload_is_invalid() throws Exception {
        var body = Map.of(
                "email", "pajaritopio@example.com",
                "password", "Abcdef12"
        );

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("201 registro válido: devuelve AuthDTOResponse y persiste usuario con ROLE_USER")
    void should_return_201_and_persist_user_with_role_user() throws Exception {
        var email = "pajaritopi0@example.com";
        var body = Map.of(
                "email", email,
                "password", "Str0ng!Pass"
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

    /**
     ********************************************
     * Tests para el endpoint /api/v1/auth/login*
     * *******************************************
     *
     */
    @Test
    @DisplayName("400 login: email con espacios internos → BAD_REQUEST con mensaje claro")
    @WithAnonymousUser
    void should_return_400_when_login_email_contains_internal_spaces() throws Exception {

        String body = """
        {"email":"pajarito pio@example.com","password":"Str0ng!Pass"}
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Email must not contain spaces"));
    }

    @Test
    @DisplayName("401 login: credenciales inválidas (password case mismatch) → UNAUTHORIZED")
    @WithAnonymousUser
    void should_return_401_when_login_password_case_is_wrong() throws Exception {

        var user = UserEntity.builder()
                .username("pajaritopi0@example.com")
                .password(passwordEncoder.encode("Str0ng!Pass"))
                .roles(Set.of())
                .build();
        userRepository.save(user);

        String body = """
    {"email":"pajaritopi0@example.com","password":"str0ng!pass"}
    """;

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("201 login: credenciales válidas → devuelve AuthDTOResponse con email normalizado")
    @WithAnonymousUser
    void should_return_201_and_authdtoresponse_when_credentials_are_valid() throws Exception {
        // Arrange
        final String email = "pajaritologin@example.com";
        final String password = "Str0ng!Pass";

        RoleEntity roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new RoleEntity(null, "ROLE_USER")));

        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser))
                .build();
        userRepository.save(user);

        // Construir payload con credenciales válidas
        String body = """
    {"email":"%s","password":"%s"}
    """.formatted(email, password);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    @DisplayName("401 login: email no registrado → UNAUTHORIZED con Problem Object")
    @WithAnonymousUser
    void should_return_401_when_login_email_does_not_exist() throws Exception {
        // email que NO existe en la BD
        String body = """
    {"email":"elgatodekaren@example.com","password":"Str0ng!Pass"}
    """.formatted(System.nanoTime()); // email único para evitar colisiones

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

}

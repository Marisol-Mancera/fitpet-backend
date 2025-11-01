package Marisol_Mancera.fitpet.auth;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.TestPropertySource;
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
 * Tests consolidados para AuthController
 * 
 * REFACTOR: Consolidación de AuthControllerTest + AuthTokenControllerTest
 * - Tests de /registro - Mantenidos sin cambios
 * - Tests de /login - Migrados de AuthTokenControllerTest y actualizados
 * - Tests de /token - Eliminados (endpoint deprecado)
 * 
 * Endpoints testeados:
 * - POST /api/v1/auth/registro → AuthDTOResponse
 * - POST /api/v1/auth/login    → TokenResponse con JWT
 */
@SpringBootTest
@TestPropertySource(properties = "api-endpoint=/api/v1")
class AuthControllerTest {

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
    }

    @BeforeEach
    void cleanupDatabase() {
        // Garantiza base de datos limpia al iniciar cada test
        // (más rápido y evita cascadas parciales)
        userRepository.deleteAllInBatch();
    }

    @BeforeEach
    void ensureRoleUserExists() {
        // Asegurar ROLE_USER (idempotente)
        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                    RoleEntity.builder().name("ROLE_USER").build()
                ));
    }

    // ========================================
    // TESTS DE REGISTRO (HU1)
    // ========================================

    @Test
    @DisplayName("201 registro válido: devuelve AuthDTOResponse y persiste usuario con ROLE_USER")
    @WithMockUser
    void should_return_201_and_persist_user_with_role_user() throws Exception {
        var email = "pajarito1@example.com";
        var payload = Map.of("email", email, "password", "Str0ng!Pass");

        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.createdAt", notNullValue()));

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
        boolean hasRoleUser = roles.stream()
                .map(r -> r.getName())
                .anyMatch("ROLE_USER"::equals);
        assertThat(hasRoleUser, is(true));
    }

    @Test
    @DisplayName("400 registro: payload inválido (sin símbolo en password)")
    @WithMockUser
    void should_return_400_when_register_payload_is_invalid() throws Exception {
        var payload = Map.of("email", "pajarito2@example.com", "password", "Abcdef12"); // sin símbolo

        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 registro: email inválido")
    @WithMockUser
    void should_return_400_when_email_is_invalid_on_register() throws Exception {
        var payload = Map.of("email", "invalid-email", "password", "Str0ng!Pass");

        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 registro: password sin símbolo")
    @WithMockUser
    void should_return_400_when_password_has_no_symbol() throws Exception {
        var payload = Map.of("email", "pajarito3@example.com", "password", "Strong0Pass"); // sin símbolo

        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("409 registro: email ya registrado")
    @WithMockUser
    void should_return_409_when_email_is_already_registered() throws Exception {
        var payload = Map.of("email", "pajarito4@example.com", "password", "Str0ng!Pass");

        // Primer registro (201)
        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        // Segundo registro con el mismo email (409)
        mockMvc.perform(post("/api/v1/auth/registro")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isConflict());
    }

    // ========================================
    // TESTS DE LOGIN (HU2)
    // ========================================

    @Test
    @DisplayName("201 login exitoso: devuelve TokenResponse con JWT")
    @WithAnonymousUser
    void should_return_201_and_token_response_when_credentials_are_valid() throws Exception {
        // Arrange: crear usuario en BD
        final String email = "pajaritologin@example.com";
        final String password = "Str0ng!Pass";

        RoleEntity roleUser = roleRepository.findByName("ROLE_USER").orElseThrow();
        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(roleUser))
                .build();
        userRepository.save(user);

        // Act & Assert
        var payload = Map.of("email", email, "password", password);

        var result = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())  // 201 Created (JWT generado)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        assertThat(result.getResponse().getContentAsString(), not(emptyOrNullString()));
    }

    @Test
    @DisplayName("400 login: email con espacios internos → BAD_REQUEST")
    @WithAnonymousUser
    void should_return_400_when_login_email_contains_spaces() throws Exception {
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
    @DisplayName("401 login: password incorrecta → UNAUTHORIZED")
    @WithAnonymousUser
    void should_return_401_when_password_is_invalid() throws Exception {
        // Arrange: crear usuario
        final String email = "pajarito5@example.com";
        RoleEntity roleUser = roleRepository.findByName("ROLE_USER").orElseThrow();
        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode("Str0ng!Pass"))
                .roles(Set.of(roleUser))
                .build();
        userRepository.save(user);

        // Act & Assert: password incorrecta
        var payload = Map.of("email", email, "password", "Wrong!Pass");

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("401 login: password case mismatch → UNAUTHORIZED")
    @WithAnonymousUser
    void should_return_401_when_login_password_case_is_wrong() throws Exception {
        // Arrange: crear usuario con password "Str0ng!Pass"
        final String email = "pajarito6@example.com";
        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode("Str0ng!Pass"))
                .roles(Set.of())
                .build();
        userRepository.save(user);

        // Act & Assert: password con case incorrecto "str0ng!pass"
        String body = """
        {"email":"%s","password":"str0ng!pass"}
        """.formatted(email);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("401 login: email no registrado → UNAUTHORIZED")
    @WithAnonymousUser
    void should_return_401_when_login_email_does_not_exist() throws Exception {
        // email que NO existe en la BD
        String body = """
        {"email":"elgatodekaren@example.com","password":"Str0ng!Pass"}
        """;

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    // ========================================
    // ENDPOINT ELIMINADO: /token
    // ========================================
    
    /**
     * ❌ TESTS ELIMINADOS: /api/v1/auth/token
     * 
     * ANTES (en AuthTokenControllerTest.java):
     * - should_return_201_and_bearer_token_on_valid_credentials()
     * - should_return_401_when_password_is_invalid()
     * 
     * RAZÓN DE ELIMINACIÓN:
     * - Endpoint /token fue deprecado y eliminado
     * - Funcionalidad movida a /login
     * - Tests equivalentes ahora prueban /login con TokenResponse
     * 
     * MIGRACIÓN:
     * - Test de 201 + JWT → should_return_201_and_token_response_when_credentials_are_valid()
     * - Test de 401 password → should_return_401_when_password_is_invalid()
     */
}
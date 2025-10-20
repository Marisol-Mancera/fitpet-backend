package Marisol_Mancera.fitpet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class JwtTokenServiceTest {

    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtDecoder jwtDecoder;
    @Autowired JwtTokenService tokenService;

    private String email;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        // Asegurar ROLE_USER
        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(RoleEntity.builder().name("ROLE_USER").build()));

        // Usuario con contraseña en BCrypt
        email = "owner+" + UUID.randomUUID() + "@example.com";
        rawPassword = "Str0ng!Pass";

        UserEntity user = UserEntity.builder()
                .username(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(Set.of(roleRepository.findByName("ROLE_USER").orElseThrow()))
                .build();

        userRepository.save(user);
    }

    @Test
    @DisplayName("Debe emitir un JWT válido cuando las credenciales son correctas")
    void should_issue_jwt_when_credentials_are_valid() {
        String token = tokenService.loginAndGenerateToken(email, rawPassword);

        assertThat(token, not(emptyOrNullString()));

        Jwt decoded = jwtDecoder.decode(token);
        assertThat(decoded.getSubject(), is(email));
        // scope esperado contiene USER (derivado de ROLE_USER)
        assertThat(String.valueOf(decoded.getClaims().get("scope")), containsString("USER"));
        // expiración presente
        assertThat(decoded.getExpiresAt(), notNullValue());
    }
}
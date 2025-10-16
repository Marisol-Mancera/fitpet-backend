package Marisol_Mancera.fitpet.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import Marisol_Mancera.fitpet.dtos.RegisterRequest;
import Marisol_Mancera.fitpet.dtos.RegisterResponse;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Debe crear el usuario con hash BCrypt y devolver RegisterResponse")
    void should_create_user_with_bcrypt_and_return_register_response() {
        // email aleatorio para evitar colisiones entre tests
        String email = "pajaritopio" + UUID.randomUUID() + "@example.com";
        String rawPassword = "Str0ng!Pass";

        RegisterResponse res = authService.register(new RegisterRequest(email, rawPassword));

        // respuesta
        assertThat(res, notNullValue());
        assertThat(res.id(), notNullValue());
        assertThat(res.email(), is(email));
        assertThat(res.createdAt(), notNullValue());

        // persistencia
        UserEntity saved = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(email))
                .findFirst()
                .orElseThrow();

        assertThat(saved.getId(), notNullValue());
        assertThat(saved.getUsername(), is(email));
        assertThat(saved.getPassword(), not(rawPassword));
        assertThat(passwordEncoder.matches(rawPassword, saved.getPassword()), is(true));
    }
}

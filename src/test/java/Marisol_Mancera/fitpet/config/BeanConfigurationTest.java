package Marisol_Mancera.fitpet.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class BeanConfigurationTest {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Debe exponer un bean PasswordEncoder (BCrypt) y validar hashes correctamente")
    void should_expose_bcrypt_password_encoder_bean_and_validate_hashes() {
        String raw = "Str0ng!Pass";

        String hash = passwordEncoder.encode(raw);

        assertThat(hash, not(is(raw)));              // el hash no es igual al texto plano
        assertThat(passwordEncoder.matches(raw, hash), is(true)); // el hash valida el raw
    }
}


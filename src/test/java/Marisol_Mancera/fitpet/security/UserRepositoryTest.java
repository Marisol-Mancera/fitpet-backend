package Marisol_Mancera.fitpet.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("Debe encontrar un usuario por username (email)")
    void should_find_user_by_username() {
        var user = UserEntity.builder()
                .username("pajaritopio@example.com")
                .password("bcrypt-hash") // valor dummy; no se valida aquí
                .build();
        userRepository.save(user);

        var found = userRepository.findByUsername("pajaritopio@example.com");

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().getUsername(), is("pajaritopio@example.com"));
    }
}


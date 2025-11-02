package Marisol_Mancera.fitpet.user;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("existsByUsername debe devolver true cuando el username ya está registrado")
    void should_return_true_when_username_exists() {
        // Arrange
        var user = UserEntity.builder()
                .username("owner@example.com")
                .password("$2a$10$bcrypt_hash") // hash de ejemplo
                .build();
        em.persistAndFlush(user);

        // Act
        boolean exists = userRepository.existsByUsername("owner@example.com");

        // Assert
        assertThat(exists, is(true));
    }
}


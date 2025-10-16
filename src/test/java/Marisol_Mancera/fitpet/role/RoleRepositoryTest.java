package Marisol_Mancera.fitpet.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Debe encontrar un rol por su nombre (findByName)")
    void should_find_role_by_name() {
        var role = RoleEntity.builder().name("ROLE_USER").build();
        em.persistAndFlush(role);

        Optional<RoleEntity> found = roleRepository.findByName("ROLE_USER");

        assertThat(found.isPresent(), is(true));
        assertThat(found.get().getName(), is("ROLE_USER"));
    }
}

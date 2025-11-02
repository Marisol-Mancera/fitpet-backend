package Marisol_Mancera.fitpet.role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
class RoleSeederTest {

    @Autowired
    RoleRepository roleRepository;

    @Test
    @DisplayName("Al iniciar la app, deben existir ROLE_USER y ROLE_ADMIN")
    void should_seed_basic_roles_on_context_startup() {
        var user = roleRepository.findByName("ROLE_USER").isPresent();
        var admin = roleRepository.findByName("ROLE_ADMIN").isPresent();

        assertThat(user, is(true));
        assertThat(admin, is(true));
    }
}

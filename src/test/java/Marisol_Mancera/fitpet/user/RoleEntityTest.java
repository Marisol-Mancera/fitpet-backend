package Marisol_Mancera.fitpet.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class RoleEntityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Debe persistir y recuperar un RoleEntity con nombre")
    void should_persist_and_load_role_entity_with_name() {
        RoleEntity role = new RoleEntity(null, "ROLE_USER");

        RoleEntity persisted = em.persistFlushFind(role);

        assertThat(persisted.getId(), notNullValue());
        assertThat(persisted.getName(), is("ROLE_USER"));
    }
}

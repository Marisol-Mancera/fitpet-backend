package Marisol_Mancera.fitpet.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import Marisol_Mancera.fitpet.role.RoleEntity;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class UserEntityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("Debe persistir UserEntity con roles (ManyToMany) y recuperar los datos")
    void should_persist_user_entity_with_roles_and_load_them() {
        //persistimos un rol y construimos el usuario con ese rol
        RoleEntity roleUser = new RoleEntity(null, "ROLE_USER");
        roleUser = em.persist(roleUser);

        UserEntity user = new UserEntity();
        user.setUsername("pajaritopio@example.com");  
        user.setPassword("$2a$10$bcrypt_hash");    // hash BCrypt
        user.setRoles(Set.of(roleUser));

        // Act
        UserEntity persisted = em.persistFlushFind(user);

        // Assert
        assertThat(persisted.getId(), notNullValue());
        assertThat(persisted.getUsername(), is("pajaritopio@example.com"));
        assertThat(persisted.getPassword(), is("$2a$10$bcrypt_hash"));
        assertThat(persisted.getRoles(), hasSize(1));
        assertThat(persisted.getRoles().iterator().next().getName(), is("ROLE_USER"));
    }
}
package Marisol_Mancera.fitpet.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.user.UserEntity;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SecurityUserTest {

    @Test
    @DisplayName("Debe exponer username, password, authorities y flags desde UserEntity")
    void should_expose_username_password_authorities_and_flags_from_user_entity() {
        // Arrange: usuario con ROLE_USER y flags activos
        RoleEntity roleUser = RoleEntity.builder().name("ROLE_USER").build();

        UserEntity user = UserEntity.builder()
                .username("owner@example.com")
                .password("$2a$10$bcrypt_hash")
                .roles(Set.of(roleUser))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();

        SecurityUser securityUser = new SecurityUser(user);

        assertThat(securityUser.getUsername(), is("owner@example.com"));
        assertThat(securityUser.getPassword(), is("$2a$10$bcrypt_hash"));

        assertThat(securityUser.getAuthorities(), not(empty()));
        assertThat(securityUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                hasItem("ROLE_USER"));

        assertThat(securityUser.isAccountNonExpired(), is(true));
        assertThat(securityUser.isAccountNonLocked(), is(true));
        assertThat(securityUser.isCredentialsNonExpired(), is(true));
        assertThat(securityUser.isEnabled(), is(true));
    }

    
}


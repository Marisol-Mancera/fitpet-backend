package Marisol_Mancera.fitpet.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.user.UserEntity;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Adaptador UserDetails para Spring Security.
 * - Expone username/password desde UserEntity.
 * - Mapea roles (RoleEntity) a authorities (ROLE_*).
 * - Devuelve los flags de estado de cuenta desde UserEntity (no constantes),
 *   habilitando políticas reales de bloqueo/expiración.
 */
public class SecurityUser implements UserDetails {

    private final UserEntity user;

    public SecurityUser(UserEntity user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = new ArrayList<GrantedAuthority>();
        if (user.getRoles() != null) {
            for (RoleEntity role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            }
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}


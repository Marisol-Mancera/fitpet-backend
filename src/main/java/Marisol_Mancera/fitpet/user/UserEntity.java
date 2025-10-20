package Marisol_Mancera.fitpet.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

import Marisol_Mancera.fitpet.role.RoleEntity;

/**
 * Entidad User alineada con el ejemplo del profesor.
 * - Lombok se usa para reducir boilerplate (constructores, getters/setters, builder).
 * - Mantiene la relación ManyToMany con RoleEntity.
 *
 * Comentarios en español para facilitar integración.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long id;

    // username = mail del usuario
    @Column(nullable = false, unique = true, length = 180)
    private String username;

    // password almacenado como hash (BCrypt)
    @Column(nullable = false)
    private String password;

    // Relación ManyToMany con roles, fetch eager 
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "roles_users",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles;

    // ----------- Flags de estado de cuenta -----------
    /** Si la cuenta no ha expirado. */
    @Builder.Default
    @Column(nullable = false)
    private boolean accountNonExpired = true;

    /** Si la cuenta no está bloqueada. */
    @Builder.Default
    @Column(nullable = false)
    private boolean accountNonLocked = true;

    /** Si las credenciales no han expirado. */
    @Builder.Default
    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    /** Si la cuenta está habilitada. */
    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;
}

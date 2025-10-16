package Marisol_Mancera.fitpet.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

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

    // Relación ManyToMany con roles, fetch eager (igual que en el ejemplo del profe)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "roles_users",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles;
}
package Marisol_Mancera.fitpet.user;


import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad RoleEntity actualizada con Lombok para coherencia con UserEntity.
 * - Usa IDENTITY para la PK.
 * - Campo name único.
 * - Compatible con la relación ManyToMany en UserEntity.
 */
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;
}


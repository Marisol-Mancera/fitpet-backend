package Marisol_Mancera.fitpet.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Acceso a datos de roles.
 * Permite buscar roles por nombre para evitar IDs mágicos.
 */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);
}

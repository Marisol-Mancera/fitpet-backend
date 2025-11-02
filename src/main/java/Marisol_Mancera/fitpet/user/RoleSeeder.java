package Marisol_Mancera.fitpet.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import Marisol_Mancera.fitpet.role.RoleEntity;
import Marisol_Mancera.fitpet.role.RoleRepository;

/**
 * Siembra los roles mínimos al arrancar la aplicación.
 * - Evita depender de IDs mágicos y de datos previos en BD.
 * - Seguro de re-ejecución: solo crea si no existen.
 */
@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        seedRoleIfMissing("ROLE_USER");
        seedRoleIfMissing("ROLE_ADMIN");
    }

    private void seedRoleIfMissing(String name) {
        roleRepository.findByName(name).orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name(name).build())
        );
    }
}

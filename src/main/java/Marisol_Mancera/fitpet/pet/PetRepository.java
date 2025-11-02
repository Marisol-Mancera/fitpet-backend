package Marisol_Mancera.fitpet.pet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio de mascotas con queries derivadas por Spring Data JPA.
 * 
 */
public interface PetRepository extends JpaRepository<PetEntity, Long> {

    /**
     * Encuentra todas las mascotas de un usuario por su username.
     * @param username - email del usuario (usado como username)
     * @return lista de mascotas del usuario
     */
    List<PetEntity> findByOwner_Username(String username);

    /**
     * Encuentra una mascota por ID solo si pertenece al usuario especificado.
     * Usado para garantizar ownership antes de operaciones sensibles (update, delete, get).
     * 
     * @param id - ID de la mascota
     * @param username - email del usuario (usado como username)
     * @return Optional con la mascota si existe y pertenece al usuario
     */
    Optional<PetEntity> findByIdAndOwner_Username(Long id, String username);
    
    /**
     * Encuentra mascotas de un usuario filtradas por especie.
     * 
     * @param username - email del usuario (usado como username)
     * @param species - especie a filtrar (ej: "Dog", "Cat")
     * @return lista de mascotas del usuario de la especie especificada
     * 
     */
    List<PetEntity> findByOwner_UsernameAndSpecies(String username, String species);
}
package Marisol_Mancera.fitpet.pet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<PetEntity, Long> {

    List<PetEntity> findByOwner_Username(String username);
 }


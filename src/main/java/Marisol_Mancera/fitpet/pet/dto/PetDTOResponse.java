// [EDIT] Añadimos el nuevo campo opcional imageUrl al record, para MVP de HU4
package Marisol_Mancera.fitpet.pet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetDTOResponse(
        Long id,
        Long ownerId,
        String name,
        String species,
        String breed,
        String sex,
        LocalDate birthDate,
        BigDecimal weightKg,
        String imageUrl
        
) { }

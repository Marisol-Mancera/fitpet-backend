package Marisol_Mancera.fitpet.pet.mapper;



import Marisol_Mancera.fitpet.pet.PetEntity;
import Marisol_Mancera.fitpet.pet.dto.PetDTOResponse;

public final class PetMapper {
    private PetMapper() {}

    public static PetDTOResponse toDTO(PetEntity e) {
        return new PetDTOResponse(
                e.getId(),
                e.getOwner().getId(),
                e.getName(),
                e.getSpecies(),
                e.getBreed(),
                e.getSex(),
                e.getBirthDate(),
                e.getWeightKg()
        );
    }
}


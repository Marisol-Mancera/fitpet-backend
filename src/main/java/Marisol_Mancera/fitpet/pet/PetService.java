package Marisol_Mancera.fitpet.pet;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import Marisol_Mancera.fitpet.pet.dto.PetCreateRequest;
import Marisol_Mancera.fitpet.user.UserEntity;
import Marisol_Mancera.fitpet.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional
    public PetEntity createForCurrentOwner(PetCreateRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        UserEntity owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        PetEntity entity = PetEntity.builder()
                .owner(owner)
                .name(req.name().trim())
                .species(req.species().trim())
                .breed(req.breed().trim())
                .sex(req.sex().trim())
                .birthDate(req.birthDate())
                .weightKg(req.weightKg())
                .build();

        return petRepository.save(entity);
    }
}


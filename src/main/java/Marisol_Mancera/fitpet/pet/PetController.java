package Marisol_Mancera.fitpet.pet;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import Marisol_Mancera.fitpet.pet.dto.PetCreateRequest;
import Marisol_Mancera.fitpet.pet.dto.PetDTOResponse;
import Marisol_Mancera.fitpet.pet.mapper.PetMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final PetRepository petRepository;

    @PostMapping
    public ResponseEntity<PetDTOResponse> create(@Valid @RequestBody PetCreateRequest request) {
        var saved = petService.createForCurrentOwner(request);
        var dto = PetMapper.toDTO(saved);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(dto);
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    @GetMapping
    public ResponseEntity<List<PetDTOResponse>> listMine() {
        String username = currentUsername(); 

        var result = petRepository.findByOwner_Username(username).stream()
                .map(PetMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDTOResponse> getById(@PathVariable Long id) {
        String username = currentUsername(); 

        var pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        return ResponseEntity.ok(PetMapper.toDTO(pet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        String username = currentUsername(); 

        PetEntity pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        petRepository.delete(pet);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDTOResponse> updateById(@PathVariable Long id,
            @RequestBody @Valid PetCreateRequest request) {
        String username = currentUsername(); 

        PetEntity pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        pet.setName(request.name().trim());
        pet.setSpecies(request.species().trim());
        pet.setBreed(request.breed().trim());
        pet.setSex(request.sex().trim());
        pet.setBirthDate(request.birthDate());
        pet.setWeightKg(request.weightKg());

        PetEntity saved = petRepository.save(pet);
        return ResponseEntity.ok(PetMapper.toDTO(saved));
    }
}

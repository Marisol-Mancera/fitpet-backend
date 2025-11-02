package Marisol_Mancera.fitpet.pet;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
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
        // Delega creación al servicio (asigna owner autenticado)
        var saved = petService.createForCurrentOwner(request);
        var dto = PetMapper.toDTO(saved);
        // Construye Location absoluta (evita hardcodear rutas)
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

    /**
     * Lista mascotas del usuario autenticado.
     * Soporta filtro opcional por especie (query param).
     * 
     * @param species (opcional) - filtra por especie (ej: "Dog", "Cat")
     * @return lista de mascotas del dueño (filtradas si se especifica especie)
     * 
     * Ejemplos de uso:
     * - GET /api/v1/pets → todas las mascotas del usuario
     * - GET /api/v1/pets?species=Dog → solo perros del usuario
     * 
     */
    @GetMapping
    public ResponseEntity<List<PetDTOResponse>> listMine(
            @RequestParam(required = false) String species) {
        String username = currentUsername();
        
        List<PetEntity> pets;
        if (species != null && !species.isBlank()) {
            // Filtro por especie (normalizado con trim para evitar errores por espacios)
            pets = petRepository.findByOwner_UsernameAndSpecies(username, species.trim());
        } else {
            // Sin filtro, todas las mascotas del usuario
            pets = petRepository.findByOwner_Username(username);
        }
        
        var result = pets.stream()
                .map(PetMapper::toDTO)
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDTOResponse> getById(@PathVariable Long id) {
        String username = currentUsername();
        // Busca solo si pertenece al dueño (seguridad por ownership)
        var pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        return ResponseEntity.ok(PetMapper.toDTO(pet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        String username = currentUsername();
        // Verifica ownership antes de eliminar
        PetEntity pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        petRepository.delete(pet);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetDTOResponse> updateById(@PathVariable Long id,
            @RequestBody @Valid PetCreateRequest request) {
        String username = currentUsername();
        // Verifica ownership antes de actualizar
        PetEntity pet = petRepository.findByIdAndOwner_Username(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        // Actualiza campos (normaliza trim en strings)
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
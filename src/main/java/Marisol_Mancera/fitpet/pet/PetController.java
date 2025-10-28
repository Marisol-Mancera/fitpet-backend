package Marisol_Mancera.fitpet.pet;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
        return ResponseEntity.created(URI.create("/api/v1/pets/" + saved.getId()))
                .body(dto);
    }

    @GetMapping
    public ResponseEntity<List<PetDTOResponse>> listMine() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // filtrado en memoria para NO tocar repositorio/servicio en este paso
        var result = petRepository.findAll().stream() // obtenemos todas
                .filter(p -> p.getOwner() != null
                && p.getOwner().getUsername() != null
                && p.getOwner().getUsername().equals(username)) // solo las del dueño
                .map(PetMapper::toDTO) // mapeo a DTO
                .collect(Collectors.toList());                            // lista final

        return ResponseEntity.ok(result);                                 // 200 OK con el array
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetDTOResponse> getById(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        PetEntity pet = petRepository.findById(id)
                .filter(p -> p.getOwner() != null && username.equals(p.getOwner().getUsername()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        return ResponseEntity.ok(PetMapper.toDTO(pet));
    }
}
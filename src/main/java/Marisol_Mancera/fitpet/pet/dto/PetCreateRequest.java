package Marisol_Mancera.fitpet.pet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

public record PetCreateRequest(
        @NotBlank String name,
        @NotBlank String species,
        @NotBlank String breed,
        @NotBlank String sex,
        @NotNull @Past LocalDate birthDate,
        @NotNull @Positive BigDecimal weightKg
) { }


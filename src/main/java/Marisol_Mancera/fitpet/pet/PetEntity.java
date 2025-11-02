package Marisol_Mancera.fitpet.pet;


import java.math.BigDecimal;
import java.time.LocalDate;

import Marisol_Mancera.fitpet.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pet")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String species;

    @NotBlank
    @Column(nullable = false)
    private String breed;

    @NotBlank
    @Column(nullable = false)
    private String sex;

    @Past
    @Column(nullable = false)
    private LocalDate birthDate;

    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weightKg;
}


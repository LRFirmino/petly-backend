package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequest(
        @NotBlank(message = "Informe o nome do pet.")
        @Size(max = 120)
        String name,

        @Size(max = 80)
        String species,

        @Size(max = 120)
        String breed,

        // SMALL | MEDIUM | LARGE
        String size,

        LocalDate birthDate,

        @Size(max = 1000)
        String notes,

        @NotNull(message = "Informe o cliente (dono) do pet.")
        Long clientId) {
}

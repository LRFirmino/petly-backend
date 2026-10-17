package com.pawsclaws.petshop.web.dto;

import java.time.LocalDate;

public record PetDto(
        Long id,
        String name,
        String species,
        String breed,
        String size,
        LocalDate birthDate,
        String notes,
        Long clientId,
        String clientName) {
}

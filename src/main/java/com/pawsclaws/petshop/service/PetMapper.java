package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Pet;
import com.pawsclaws.petshop.web.dto.PetDto;

public final class PetMapper {

    private PetMapper() {
    }

    public static PetDto toDto(Pet pet) {
        return new PetDto(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getSize() != null ? pet.getSize().name() : null,
                pet.getBirthDate(),
                pet.getNotes(),
                pet.getClient() != null ? pet.getClient().getId() : null,
                pet.getClient() != null ? pet.getClient().getName() : null);
    }
}

package com.pawsclaws.petshop.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClientDto(
        Long id,
        String name,
        String email,
        String phone,
        String notes,
        List<PetDto> pets,
        long visits,
        LocalDateTime lastVisit) {
}

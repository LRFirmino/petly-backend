package com.pawsclaws.petshop.web.dto;

public record PetshopSettingsDto(
        Long id,
        String name,
        String slug,
        String email,
        String phone,
        String address,
        String cnpj,
        String logoUrl) {
}

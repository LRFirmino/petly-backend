package com.pawsclaws.petshop.web.dto;

public record PlatformPetshopDto(
        Long id,
        String name,
        String slug,
        String email,
        String phone,
        String address,
        boolean active,
        long users,
        long clients,
        long pets,
        long appointments) {
}

package com.pawsclaws.petshop.auth.dto;

public record UserSummary(
        Long id,
        String name,
        String email,
        String role,
        PetshopSummary petshop) {
}

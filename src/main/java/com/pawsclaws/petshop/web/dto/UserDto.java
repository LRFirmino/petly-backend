package com.pawsclaws.petshop.web.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        boolean active,
        Instant createdAt) {
}

package com.pawsclaws.petshop.web.dto;

public record TeamMemberDto(
        Long id,
        String name,
        String role,
        String specialty,
        String rating,
        Integer goal,
        long todayCount,
        int capacityPct) {
}

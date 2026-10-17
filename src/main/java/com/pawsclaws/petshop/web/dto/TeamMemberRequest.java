package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamMemberRequest(
        @NotBlank(message = "Informe o nome do profissional.")
        @Size(max = 160)
        String name,

        @Size(max = 120)
        String role,

        @Size(max = 160)
        String specialty,

        @Size(max = 10)
        String rating,

        Integer dailyCapacity) {
}

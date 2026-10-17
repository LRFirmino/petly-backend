package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 160)
        String name,

        // ADMIN | STAFF
        String role,

        Boolean active,

        // Opcional: só troca a senha se vier preenchida.
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        String password) {
}

package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "Informe o nome.")
        @Size(max = 160)
        String name,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 160)
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        String password,

        // ADMIN | STAFF (padrão: STAFF)
        String role) {
}

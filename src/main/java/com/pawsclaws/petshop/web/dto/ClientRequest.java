package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequest(
        @NotBlank(message = "Informe o nome do cliente.")
        @Size(max = 160)
        String name,

        @Email(message = "E-mail inválido.")
        @Size(max = 160)
        String email,

        @Size(max = 40)
        String phone,

        @Size(max = 1000)
        String notes) {
}

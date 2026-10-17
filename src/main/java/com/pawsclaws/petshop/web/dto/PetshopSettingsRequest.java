package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PetshopSettingsRequest(
        @NotBlank(message = "Informe o nome do estabelecimento.")
        @Size(max = 160)
        String name,

        @Email(message = "E-mail inválido.")
        @Size(max = 160)
        String email,

        @Size(max = 40)
        String phone,

        @Size(max = 240)
        String address,

        @Size(max = 20)
        String cnpj,

        // Optional data URL (base64) of the company logo.
        String logoUrl) {
}

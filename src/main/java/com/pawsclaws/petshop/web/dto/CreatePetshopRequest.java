package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Criação de um novo petshop (tenant) + sua primeira conta ADMIN.
 * Restrito ao administrador da plataforma.
 */
public record CreatePetshopRequest(
        @NotBlank(message = "Informe o nome do petshop.")
        @Size(max = 160)
        String petshopName,

        @Size(max = 40)
        String phone,

        @Email(message = "E-mail inválido.")
        @Size(max = 160)
        String email,

        @Size(max = 240)
        String address,

        @NotBlank(message = "Informe o nome do responsável.")
        @Size(max = 160)
        String adminName,

        @NotBlank(message = "Informe o e-mail do responsável.")
        @Email(message = "E-mail inválido.")
        @Size(max = 160)
        String adminEmail,

        @NotBlank(message = "Informe a senha do responsável.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        String adminPassword) {
}

package com.pawsclaws.petshop.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-service signup: creates a brand new petshop (tenant) and its first
 * ADMIN user. The new tenant starts with zero business data.
 */
public record RegisterRequest(
        @NotBlank(message = "Informe o nome do petshop.")
        @Size(max = 120)
        String petshopName,

        @NotBlank(message = "Informe o seu nome.")
        @Size(max = 120)
        String name,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
        String password) {
}

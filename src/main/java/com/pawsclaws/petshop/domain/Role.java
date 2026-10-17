package com.pawsclaws.petshop.domain;

/**
 * Papéis. {@code ADMIN} e {@code STAFF} são escopados a um petshop;
 * {@code PLATFORM_ADMIN} é o administrador da plataforma (equipe Petly) e
 * gerencia os petshops/contas, sem pertencer a um petshop real.
 */
public enum Role {
    ADMIN,
    STAFF,
    PLATFORM_ADMIN
}

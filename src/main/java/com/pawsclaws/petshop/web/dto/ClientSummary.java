package com.pawsclaws.petshop.web.dto;

public record ClientSummary(
        long activeClients,
        long registeredPets,
        long upcomingBirthdays) {
}

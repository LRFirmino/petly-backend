package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.security.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * Single entry point for resolving the tenant of the current request.
 * Always sourced from the authenticated principal / JWT, never from input.
 */
@Service
public class TenantService {

    private final PetshopRepository petshopRepository;

    public TenantService(PetshopRepository petshopRepository) {
        this.petshopRepository = petshopRepository;
    }

    public Long currentPetshopId() {
        return SecurityUtils.currentPetshopId();
    }

    /**
     * Returns a lightweight reference to the current tenants entity, suitable
     * for assigning as a relation without an extra SELECT.
     */
    public Petshop currentPetshopReference() {
        return petshopRepository.getReferenceById(currentPetshopId());
    }
}

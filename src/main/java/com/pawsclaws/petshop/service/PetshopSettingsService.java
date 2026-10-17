package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Petshop;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.PetshopRepository;
import com.pawsclaws.petshop.web.dto.PetshopSettingsDto;
import com.pawsclaws.petshop.web.dto.PetshopSettingsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetshopSettingsService {

    private final PetshopRepository petshopRepository;
    private final TenantService tenantService;

    public PetshopSettingsService(PetshopRepository petshopRepository, TenantService tenantService) {
        this.petshopRepository = petshopRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public PetshopSettingsDto get() {
        return toDto(requirePetshop());
    }

    @Transactional
    public PetshopSettingsDto update(PetshopSettingsRequest request) {
        Petshop petshop = requirePetshop();
        petshop.setName(request.name().trim());
        petshop.setEmail(trimToNull(request.email()));
        petshop.setPhone(trimToNull(request.phone()));
        petshop.setAddress(trimToNull(request.address()));
        petshop.setCnpj(trimToNull(request.cnpj()));
        if (request.logoUrl() != null) {
            petshop.setLogoUrl(request.logoUrl().isEmpty() ? null : request.logoUrl());
        }
        petshopRepository.save(petshop);
        return toDto(petshop);
    }

    private Petshop requirePetshop() {
        return petshopRepository.findById(tenantService.currentPetshopId())
                .orElseThrow(() -> new ResourceNotFoundException("Petshop não encontrado."));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static PetshopSettingsDto toDto(Petshop petshop) {
        return new PetshopSettingsDto(
                petshop.getId(),
                petshop.getName(),
                petshop.getSlug(),
                petshop.getEmail(),
                petshop.getPhone(),
                petshop.getAddress(),
                petshop.getCnpj(),
                petshop.getLogoUrl());
    }
}

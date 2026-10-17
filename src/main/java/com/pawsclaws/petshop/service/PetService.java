package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Client;
import com.pawsclaws.petshop.domain.Pet;
import com.pawsclaws.petshop.domain.SizeTier;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.web.dto.PetDto;
import com.pawsclaws.petshop.web.dto.PetRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class PetService {

    private final PetRepository petRepository;
    private final ClientRepository clientRepository;
    private final TenantService tenantService;

    public PetService(PetRepository petRepository,
                      ClientRepository clientRepository,
                      TenantService tenantService) {
        this.petRepository = petRepository;
        this.clientRepository = clientRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<PetDto> findAll() {
        Long petshopId = tenantService.currentPetshopId();
        return petRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(PetMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PetDto> findByClient(Long clientId) {
        Long petshopId = tenantService.currentPetshopId();
        requireClient(clientId, petshopId);
        return petRepository.findAllByPetshopIdAndClientIdOrderByNameAsc(petshopId, clientId).stream()
                .map(PetMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetDto findById(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        return PetMapper.toDto(requirePet(id, petshopId));
    }

    @Transactional
    public PetDto create(PetRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Pet pet = new Pet();
        pet.setPetshop(tenantService.currentPetshopReference());
        apply(pet, request, petshopId);
        petRepository.save(pet);
        return PetMapper.toDto(pet);
    }

    @Transactional
    public PetDto update(Long id, PetRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Pet pet = requirePet(id, petshopId);
        apply(pet, request, petshopId);
        petRepository.save(pet);
        return PetMapper.toDto(pet);
    }

    @Transactional
    public void delete(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        petRepository.delete(requirePet(id, petshopId));
    }

    Pet requirePet(Long id, Long petshopId) {
        return petRepository.findByIdAndPetshopId(id, petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado."));
    }

    private Client requireClient(Long clientId, Long petshopId) {
        return clientRepository.findByIdAndPetshopId(clientId, petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private void apply(Pet pet, PetRequest request, Long petshopId) {
        Client owner = requireClient(request.clientId(), petshopId);
        pet.setClient(owner);
        pet.setName(request.name().trim());
        pet.setSpecies(request.species());
        pet.setBreed(request.breed());
        pet.setSize(parseSize(request.size()));
        pet.setBirthDate(request.birthDate());
        pet.setNotes(request.notes());
    }

    private SizeTier parseSize(String size) {
        if (size == null || size.isBlank()) {
            return null;
        }
        try {
            return SizeTier.valueOf(size.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tamanho inválido. Use SMALL, MEDIUM ou LARGE.");
        }
    }
}

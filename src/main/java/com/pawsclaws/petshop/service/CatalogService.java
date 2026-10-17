package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Combo;
import com.pawsclaws.petshop.domain.ServiceItem;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.ComboRepository;
import com.pawsclaws.petshop.repository.ServiceItemRepository;
import com.pawsclaws.petshop.web.dto.ComboDto;
import com.pawsclaws.petshop.web.dto.ComboRequest;
import com.pawsclaws.petshop.web.dto.ServiceDto;
import com.pawsclaws.petshop.web.dto.ServiceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class CatalogService {

    private final ServiceItemRepository serviceRepository;
    private final ComboRepository comboRepository;
    private final TenantService tenantService;

    public CatalogService(ServiceItemRepository serviceRepository,
                          ComboRepository comboRepository,
                          TenantService tenantService) {
        this.serviceRepository = serviceRepository;
        this.comboRepository = comboRepository;
        this.tenantService = tenantService;
    }

    // ---- Serviços -----------------------------------------------------

    @Transactional(readOnly = true)
    public List<ServiceDto> listServices() {
        return serviceRepository.findAllByPetshopIdOrderByNameAsc(tenantService.currentPetshopId()).stream()
                .map(CatalogService::toServiceDto)
                .toList();
    }

    @Transactional
    public ServiceDto createService(ServiceRequest request) {
        ServiceItem service = new ServiceItem();
        service.setPetshop(tenantService.currentPetshopReference());
        applyService(service, request);
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public ServiceDto updateService(Long id, ServiceRequest request) {
        ServiceItem service = requireService(id);
        applyService(service, request);
        return toServiceDto(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(Long id) {
        serviceRepository.delete(requireService(id));
    }

    private ServiceItem requireService(Long id) {
        return serviceRepository.findByIdAndPetshopId(id, tenantService.currentPetshopId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
    }

    private static void applyService(ServiceItem service, ServiceRequest request) {
        service.setName(request.name().trim());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPriceSmall(request.priceSmall());
        service.setPriceMedium(request.priceMedium());
        service.setPriceLarge(request.priceLarge());
    }

    // ---- Combos -------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ComboDto> listCombos() {
        return comboRepository.findAllByPetshopIdOrderByPopularityDesc(tenantService.currentPetshopId()).stream()
                .map(CatalogService::toComboDto)
                .toList();
    }

    @Transactional
    public ComboDto createCombo(ComboRequest request) {
        Combo combo = new Combo();
        combo.setPetshop(tenantService.currentPetshopReference());
        applyCombo(combo, request);
        return toComboDto(comboRepository.save(combo));
    }

    @Transactional
    public ComboDto updateCombo(Long id, ComboRequest request) {
        Combo combo = requireCombo(id);
        applyCombo(combo, request);
        return toComboDto(comboRepository.save(combo));
    }

    @Transactional
    public void deleteCombo(Long id) {
        comboRepository.delete(requireCombo(id));
    }

    private Combo requireCombo(Long id) {
        return comboRepository.findByIdAndPetshopId(id, tenantService.currentPetshopId())
                .orElseThrow(() -> new ResourceNotFoundException("Combo não encontrado."));
    }

    private static void applyCombo(Combo combo, ComboRequest request) {
        combo.setName(request.name().trim());
        combo.setDescription(request.description());
        combo.setPriceFrom(request.priceFrom());
        combo.setPopularity(request.popularity());
    }

    // ---- Mapeamento ---------------------------------------------------

    static ServiceDto toServiceDto(ServiceItem service) {
        return new ServiceDto(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                Arrays.asList(service.getPriceSmall(), service.getPriceMedium(), service.getPriceLarge()));
    }

    static ComboDto toComboDto(Combo combo) {
        return new ComboDto(
                combo.getId(),
                combo.getName(),
                combo.getDescription(),
                combo.getPriceFrom(),
                combo.getPopularity());
    }
}

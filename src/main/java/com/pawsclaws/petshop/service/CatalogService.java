package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.AppointmentStatus;
import com.pawsclaws.petshop.domain.CatalogItemType;
import com.pawsclaws.petshop.domain.Combo;
import com.pawsclaws.petshop.domain.ServiceItem;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ComboRepository;
import com.pawsclaws.petshop.repository.ServiceItemRepository;
import com.pawsclaws.petshop.web.dto.ComboDto;
import com.pawsclaws.petshop.web.dto.ComboRequest;
import com.pawsclaws.petshop.web.dto.ServiceDto;
import com.pawsclaws.petshop.web.dto.ServiceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class CatalogService {

    private final ServiceItemRepository serviceRepository;
    private final ComboRepository comboRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;

    public CatalogService(ServiceItemRepository serviceRepository,
                          ComboRepository comboRepository,
                          AppointmentRepository appointmentRepository,
                          TenantService tenantService) {
        this.serviceRepository = serviceRepository;
        this.comboRepository = comboRepository;
        this.appointmentRepository = appointmentRepository;
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
        Long petshopId = tenantService.currentPetshopId();
        return comboRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(combo -> toComboDto(combo, petshopId))
                .sorted(Comparator.comparingLong(ComboDto::popularity).reversed()
                        .thenComparing(ComboDto::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public ComboDto createCombo(ComboRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Combo combo = new Combo();
        combo.setPetshop(tenantService.currentPetshopReference());
        applyCombo(combo, request);
        return toComboDto(comboRepository.save(combo), petshopId);
    }

    @Transactional
    public ComboDto updateCombo(Long id, ComboRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Combo combo = requireCombo(id);
        applyCombo(combo, request);
        return toComboDto(comboRepository.save(combo), petshopId);
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
        combo.setDurationMinutes(request.durationMinutes());
        combo.setPriceSmall(request.priceSmall());
        combo.setPriceMedium(request.priceMedium());
        combo.setPriceLarge(request.priceLarge());
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

    private ComboDto toComboDto(Combo combo, Long petshopId) {
        long popularity = appointmentRepository.countByPetshopIdAndItemTypeAndItemIdAndStatusNot(
                petshopId, CatalogItemType.COMBO, combo.getId(), AppointmentStatus.CANCELLED);
        return new ComboDto(
                combo.getId(),
                combo.getName(),
                combo.getDescription(),
                combo.getDurationMinutes(),
                Arrays.asList(combo.getPriceSmall(), combo.getPriceMedium(), combo.getPriceLarge()),
                CatalogPricing.minPrice(combo),
                popularity);
    }
}

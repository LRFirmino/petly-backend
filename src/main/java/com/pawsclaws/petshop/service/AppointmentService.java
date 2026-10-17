package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.domain.AppointmentStatus;
import com.pawsclaws.petshop.domain.CatalogItemType;
import com.pawsclaws.petshop.domain.Client;
import com.pawsclaws.petshop.domain.Combo;
import com.pawsclaws.petshop.domain.Pet;
import com.pawsclaws.petshop.domain.ServiceItem;
import com.pawsclaws.petshop.domain.SizeTier;
import com.pawsclaws.petshop.exception.ConflictException;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.ComboRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.repository.ServiceItemRepository;
import com.pawsclaws.petshop.web.dto.AppointmentDto;
import com.pawsclaws.petshop.web.dto.AppointmentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final ServiceItemRepository serviceRepository;
    private final ComboRepository comboRepository;
    private final TenantService tenantService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ClientRepository clientRepository,
                              PetRepository petRepository,
                              ServiceItemRepository serviceRepository,
                              ComboRepository comboRepository,
                              TenantService tenantService) {
        this.appointmentRepository = appointmentRepository;
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.serviceRepository = serviceRepository;
        this.comboRepository = comboRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> findAll() {
        Long petshopId = tenantService.currentPetshopId();
        return appointmentRepository.findAllByPetshopIdOrderByScheduledAtAsc(petshopId).stream()
                .map(AppointmentService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> findBetween(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        Long petshopId = tenantService.currentPetshopId();
        return appointmentRepository
                .findAllByPetshopIdAndScheduledAtBetweenOrderByScheduledAtAsc(petshopId, from, to).stream()
                .map(AppointmentService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentDto findById(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        return toDto(requireAppointment(id, petshopId));
    }

    @Transactional
    public AppointmentDto create(AppointmentRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Appointment appointment = new Appointment();
        appointment.setPetshop(tenantService.currentPetshopReference());
        apply(appointment, request, petshopId);
        assertNoOverlap(petshopId, null, appointment);
        appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    @Transactional
    public AppointmentDto update(Long id, AppointmentRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Appointment appointment = requireAppointment(id, petshopId);
        apply(appointment, request, petshopId);
        assertNoOverlap(petshopId, id, appointment);
        appointmentRepository.save(appointment);
        return toDto(appointment);
    }

    /**
     * Regra de negócio: um profissional não pode ter dois atendimentos com
     * horários sobrepostos. O intervalo é [início, início + duração).
     * Atendimentos cancelados não ocupam a agenda.
     */
    private void assertNoOverlap(Long petshopId, Long excludeId, Appointment candidate) {
        String staff = candidate.getStaffName();
        LocalDateTime start = candidate.getScheduledAt();
        if (staff == null || staff.isBlank() || start == null) {
            return;
        }
        LocalDateTime end = start.plusMinutes(durationOf(candidate));

        appointmentRepository.findAllByPetshopIdAndStaffNameIgnoreCase(petshopId, staff.trim()).stream()
                .filter(other -> other.getStatus() != AppointmentStatus.CANCELLED)
                .filter(other -> excludeId == null || !excludeId.equals(other.getId()))
                .filter(other -> overlaps(start, end, other.getScheduledAt(), other.getScheduledAt().plusMinutes(durationOf(other))))
                .findFirst()
                .ifPresent(conflict -> {
                    throw new ConflictException(String.format(
                            "%s já tem um atendimento das %s às %s. Escolha outro horário.",
                            staff.trim(),
                            time(conflict.getScheduledAt()),
                            time(conflict.getScheduledAt().plusMinutes(durationOf(conflict)))));
                });
    }

    private static boolean overlaps(LocalDateTime startA, LocalDateTime endA,
                                    LocalDateTime startB, LocalDateTime endB) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private static long durationOf(Appointment appointment) {
        Integer duration = appointment.getDurationMinutes();
        return duration != null && duration > 0 ? duration : 60;
    }

    private static String time(LocalDateTime moment) {
        return moment.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Transactional
    public void delete(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        appointmentRepository.delete(requireAppointment(id, petshopId));
    }

    private Appointment requireAppointment(Long id, Long petshopId) {
        return appointmentRepository.findByIdAndPetshopId(id, petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado."));
    }

    private void apply(Appointment appointment, AppointmentRequest request, Long petshopId) {
        Client client = clientRepository.findByIdAndPetshopId(request.clientId(), petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        Pet pet = null;
        if (request.petId() != null) {
            pet = petRepository.findByIdAndPetshopId(request.petId(), petshopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado."));
            if (!pet.getClient().getId().equals(client.getId())) {
                throw new IllegalArgumentException("O pet selecionado não pertence a este cliente.");
            }
        }

        appointment.setClient(client);
        appointment.setPet(pet);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setStatus(parseStatus(request.status()));
        appointment.setStaffName(request.staffName());
        appointment.setNotes(request.notes());
        applyCatalogItem(appointment, request, petshopId, pet);
    }

    /**
     * Vincula o agendamento a um serviço ou combo (quando informado) e resolve
     * duração/preço. Valores explícitos na requisição têm prioridade; caso
     * contrário a duração vem do item e o preço do porte do pet.
     *
     * <p>Sem {@code itemType}/{@code itemId} mantém o comportamento legado
     * (rótulo em texto livre) e preserva um vínculo já existente em edições.</p>
     */
    private void applyCatalogItem(Appointment appointment, AppointmentRequest request,
                                  Long petshopId, Pet pet) {
        CatalogItemType type = parseItemType(request.itemType());
        Long itemId = request.itemId();

        if (type == null && itemId == null) {
            String label = request.service() == null ? null : request.service().trim();
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("Informe o serviço.");
            }
            appointment.setService(label);
            appointment.setDurationMinutes(request.durationMinutes());
            appointment.setPrice(request.price());
            return;
        }
        if (type == null || itemId == null) {
            throw new IllegalArgumentException("Informe o tipo (SERVICE/COMBO) e o item de catálogo.");
        }

        String name;
        Integer itemDuration;
        BigDecimal itemPrice;
        if (type == CatalogItemType.SERVICE) {
            ServiceItem service = serviceRepository.findByIdAndPetshopId(itemId, petshopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));
            name = service.getName();
            itemDuration = service.getDurationMinutes();
            itemPrice = CatalogPricing.priceForSize(service, sizeTier(pet));
        } else {
            Combo combo = comboRepository.findByIdAndPetshopId(itemId, petshopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Combo não encontrado."));
            name = combo.getName();
            itemDuration = combo.getDurationMinutes();
            itemPrice = CatalogPricing.priceForSize(combo, sizeTier(pet));
        }

        appointment.setItemType(type);
        appointment.setItemId(itemId);
        appointment.setService(name);
        appointment.setDurationMinutes(request.durationMinutes() != null ? request.durationMinutes() : itemDuration);
        appointment.setPrice(request.price() != null ? request.price() : itemPrice);
    }

    private static SizeTier sizeTier(Pet pet) {
        return pet != null ? pet.getSize() : null;
    }

    private static CatalogItemType parseItemType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CatalogItemType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de item inválido. Use SERVICE ou COMBO.");
        }
    }

    private AppointmentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return AppointmentStatus.SCHEDULED;
        }
        try {
            return AppointmentStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Status inválido. Use SCHEDULED, IN_PROGRESS, COMPLETED ou CANCELLED.");
        }
    }

    static AppointmentDto toDto(Appointment appointment) {
        java.time.LocalDateTime endTime = null;
        if (appointment.getScheduledAt() != null && appointment.getDurationMinutes() != null) {
            endTime = appointment.getScheduledAt().plusMinutes(appointment.getDurationMinutes());
        }
        Pet pet = appointment.getPet();
        return new AppointmentDto(
                appointment.getId(),
                appointment.getClient() != null ? appointment.getClient().getId() : null,
                appointment.getClient() != null ? appointment.getClient().getName() : null,
                pet != null ? pet.getId() : null,
                pet != null ? pet.getName() : null,
                pet != null ? pet.getBreed() : null,
                pet != null && pet.getSize() != null ? pet.getSize().name() : null,
                appointment.getItemType() != null ? appointment.getItemType().name() : null,
                appointment.getItemId(),
                appointment.getService(),
                appointment.getScheduledAt(),
                endTime,
                appointment.getDurationMinutes(),
                appointment.getPrice(),
                appointment.getStatus().name(),
                appointment.getStaffName(),
                appointment.getNotes());
    }
}

package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.domain.Client;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.web.dto.ClientDto;
import com.pawsclaws.petshop.web.dto.ClientRequest;
import com.pawsclaws.petshop.web.dto.ClientSummary;
import com.pawsclaws.petshop.web.dto.PetDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * All operations are implicitly scoped to the petshop of the authenticated
 * user. A client id from another petshop simply "does not exist" here (404).
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;

    public ClientService(ClientRepository clientRepository,
                         PetRepository petRepository,
                         AppointmentRepository appointmentRepository,
                         TenantService tenantService) {
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        Long petshopId = tenantService.currentPetshopId();
        return clientRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(client -> toDto(client, petshopId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientSummary summary() {
        Long petshopId = tenantService.currentPetshopId();
        long upcomingBirthdays = petRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .filter(pet -> hasBirthdayWithin(pet.getBirthDate(), java.time.LocalDate.now(), 7))
                .count();
        return new ClientSummary(
                clientRepository.countByPetshopId(petshopId),
                petRepository.countByPetshopId(petshopId),
                upcomingBirthdays);
    }

    private static boolean hasBirthdayWithin(java.time.LocalDate birthDate,
                                             java.time.LocalDate today,
                                             int days) {
        if (birthDate == null) {
            return false;
        }
        for (int offset = 0; offset <= days; offset++) {
            java.time.LocalDate day = today.plusDays(offset);
            if (birthDate.getMonthValue() == day.getMonthValue() && birthDate.getDayOfMonth() == day.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        return toDto(requireClient(id, petshopId), petshopId);
    }

    @Transactional
    public ClientDto create(ClientRequest request) {
        Long petshopId = tenantService.currentPetshopId();

        Client client = new Client();
        client.setPetshop(tenantService.currentPetshopReference());
        apply(client, request);
        clientRepository.save(client);

        return toDto(client, petshopId);
    }

    @Transactional
    public ClientDto update(Long id, ClientRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        Client client = requireClient(id, petshopId);
        apply(client, request);
        clientRepository.save(client);
        return toDto(client, petshopId);
    }

    @Transactional
    public void delete(Long id) {
        Long petshopId = tenantService.currentPetshopId();
        Client client = requireClient(id, petshopId);
        clientRepository.delete(client);
    }

    private Client requireClient(Long id, Long petshopId) {
        return clientRepository.findByIdAndPetshopId(id, petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));
    }

    private void apply(Client client, ClientRequest request) {
        client.setName(request.name().trim());
        client.setEmail(request.email() != null ? request.email().trim() : null);
        client.setPhone(request.phone() != null ? request.phone().trim() : null);
        client.setNotes(request.notes());
    }

    private ClientDto toDto(Client client, Long petshopId) {
        List<PetDto> pets =
                petRepository.findAllByPetshopIdAndClientIdOrderByNameAsc(petshopId, client.getId())
                        .stream()
                        .map(PetMapper::toDto)
                        .toList();
        long visits = appointmentRepository.countByPetshopIdAndClientId(petshopId, client.getId());
        LocalDateTime lastVisit = appointmentRepository
                .findFirstByPetshopIdAndClientIdOrderByScheduledAtDesc(petshopId, client.getId())
                .map(Appointment::getScheduledAt)
                .orElse(null);
        return new ClientDto(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getNotes(),
                pets,
                visits,
                lastVisit);
    }
}

package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.ClientRepository;
import com.pawsclaws.petshop.repository.PetRepository;
import com.pawsclaws.petshop.security.SecurityUtils;
import com.pawsclaws.petshop.web.dto.AppointmentDto;
import com.pawsclaws.petshop.web.dto.DashboardSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DashboardService {

    private final ClientRepository clientRepository;
    private final PetRepository petRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;

    public DashboardService(ClientRepository clientRepository,
                            PetRepository petRepository,
                            AppointmentRepository appointmentRepository,
                            TenantService tenantService) {
        this.clientRepository = clientRepository;
        this.petRepository = petRepository;
        this.appointmentRepository = appointmentRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        Long petshopId = tenantService.currentPetshopId();
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<AppointmentDto> todaySchedule = appointmentRepository
                .findAllByPetshopIdAndScheduledAtBetweenOrderByScheduledAtAsc(petshopId, startOfDay, endOfDay)
                .stream()
                .map(AppointmentService::toDto)
                .toList();

        long inProgress = todaySchedule.stream()
                .filter(appointment -> "IN_PROGRESS".equals(appointment.status()))
                .count();

        BigDecimal revenueToday = todaySchedule.stream()
                .map(appointment -> appointment.price() != null ? appointment.price() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant startOfMonth = today.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        return new DashboardSummary(
                SecurityUtils.currentUser().getPetshopName(),
                clientRepository.countByPetshopId(petshopId),
                petRepository.countByPetshopId(petshopId),
                appointmentRepository.countByPetshopId(petshopId),
                todaySchedule.size(),
                inProgress,
                clientRepository.countByPetshopIdAndCreatedAtAfter(petshopId, startOfMonth),
                revenueToday,
                todaySchedule);
    }
}

package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.domain.TeamMember;
import com.pawsclaws.petshop.exception.ResourceNotFoundException;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.repository.TeamMemberRepository;
import com.pawsclaws.petshop.web.dto.TeamMemberDto;
import com.pawsclaws.petshop.web.dto.TeamMemberRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamService {

    private final TeamMemberRepository teamRepository;
    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;

    public TeamService(TeamMemberRepository teamRepository,
                       AppointmentRepository appointmentRepository,
                       TenantService tenantService) {
        this.teamRepository = teamRepository;
        this.appointmentRepository = appointmentRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public List<TeamMemberDto> list() {
        Long petshopId = tenantService.currentPetshopId();
        List<Appointment> today = todayAppointments(petshopId);
        return teamRepository.findAllByPetshopIdOrderByNameAsc(petshopId).stream()
                .map(member -> toDto(member, today))
                .toList();
    }

    @Transactional
    public TeamMemberDto create(TeamMemberRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        TeamMember member = new TeamMember();
        member.setPetshop(tenantService.currentPetshopReference());
        apply(member, request);
        teamRepository.save(member);
        return toDto(member, todayAppointments(petshopId));
    }

    @Transactional
    public TeamMemberDto update(Long id, TeamMemberRequest request) {
        Long petshopId = tenantService.currentPetshopId();
        TeamMember member = requireMember(id, petshopId);
        apply(member, request);
        teamRepository.save(member);
        return toDto(member, todayAppointments(petshopId));
    }

    @Transactional
    public void delete(Long id) {
        teamRepository.delete(requireMember(id, tenantService.currentPetshopId()));
    }

    private List<Appointment> todayAppointments(Long petshopId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return appointmentRepository.findAllByPetshopIdAndScheduledAtBetweenOrderByScheduledAtAsc(
                petshopId, startOfDay, startOfDay.plusDays(1));
    }

    private TeamMember requireMember(Long id, Long petshopId) {
        return teamRepository.findByIdAndPetshopId(id, petshopId)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado."));
    }

    private static void apply(TeamMember member, TeamMemberRequest request) {
        member.setName(request.name().trim());
        member.setRole(request.role());
        member.setSpecialty(request.specialty());
        member.setRating(request.rating());
        member.setDailyCapacity(request.dailyCapacity());
    }

    private static TeamMemberDto toDto(TeamMember member, List<Appointment> today) {
        long todayCount = today.stream()
                .filter(appointment -> member.getName().equals(appointment.getStaffName()))
                .count();
        int goal = member.getDailyCapacity() != null ? member.getDailyCapacity() : 0;
        int capacityPct = goal > 0 ? (int) Math.round(todayCount * 100.0 / goal) : 0;
        return new TeamMemberDto(
                member.getId(),
                member.getName(),
                member.getRole(),
                member.getSpecialty(),
                member.getRating(),
                goal,
                todayCount,
                capacityPct);
    }
}

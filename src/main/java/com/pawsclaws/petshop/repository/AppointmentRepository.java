package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.domain.AppointmentStatus;
import com.pawsclaws.petshop.domain.CatalogItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAllByPetshopIdOrderByScheduledAtAsc(Long petshopId);

    Optional<Appointment> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopIdAndClientId(Long petshopId, Long clientId);

    /** Quantidade de agendamentos de um item de catálogo (usada como "popularidade" real). */
    long countByPetshopIdAndItemTypeAndItemIdAndStatusNot(
            Long petshopId, CatalogItemType itemType, Long itemId, AppointmentStatus status);

    java.util.Optional<Appointment> findFirstByPetshopIdAndClientIdOrderByScheduledAtDesc(
            Long petshopId, Long clientId);

    java.util.List<Appointment> findAllByPetshopIdAndStaffNameIgnoreCase(Long petshopId, String staffName);

    long countByPetshopIdAndStatus(Long petshopId, AppointmentStatus status);

    List<Appointment> findAllByPetshopIdAndScheduledAtBetweenOrderByScheduledAtAsc(
            Long petshopId, LocalDateTime from, LocalDateTime to);

    long countByPetshopId(Long petshopId);
}

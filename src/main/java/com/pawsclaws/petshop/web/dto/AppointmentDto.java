package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentDto(
        Long id,
        Long clientId,
        String clientName,
        Long petId,
        String petName,
        String petBreed,
        String petSize,
        String itemType,
        Long itemId,
        String service,
        LocalDateTime scheduledAt,
        LocalDateTime endTime,
        Integer durationMinutes,
        BigDecimal price,
        String status,
        String staffName,
        String notes) {
}

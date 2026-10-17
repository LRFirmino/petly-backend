package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "Informe o cliente.")
        Long clientId,

        Long petId,

        @NotBlank(message = "Informe o serviço.")
        @Size(max = 160)
        String service,

        @NotNull(message = "Informe a data e hora.")
        LocalDateTime scheduledAt,

        // SCHEDULED | IN_PROGRESS | COMPLETED | CANCELLED
        String status,

        @Size(max = 160)
        String staffName,

        Integer durationMinutes,

        BigDecimal price,

        @Size(max = 1000)
        String notes) {
}

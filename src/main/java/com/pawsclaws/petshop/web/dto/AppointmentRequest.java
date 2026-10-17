package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "Informe o cliente.")
        Long clientId,

        Long petId,

        // SERVICE | COMBO — quando presente, o agendamento é vinculado ao item de catálogo.
        String itemType,

        Long itemId,

        // Rótulo exibido. Opcional quando itemType/itemId identificam o item.
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

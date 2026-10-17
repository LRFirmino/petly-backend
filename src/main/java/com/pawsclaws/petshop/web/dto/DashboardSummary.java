package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummary(
        String petshopName,
        long clients,
        long pets,
        long appointments,
        long todayAppointments,
        long inProgress,
        long newClientsThisMonth,
        BigDecimal revenueToday,
        List<AppointmentDto> todaySchedule) {
}

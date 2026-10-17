package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.Appointment;
import com.pawsclaws.petshop.repository.AppointmentRepository;
import com.pawsclaws.petshop.web.dto.ReportsSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final String[] MONTHS = {
            "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    };
    private static final String[] MONTHS_LONG = {
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    };

    private final AppointmentRepository appointmentRepository;
    private final TenantService tenantService;

    public ReportsService(AppointmentRepository appointmentRepository, TenantService tenantService) {
        this.appointmentRepository = appointmentRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public ReportsSummary summary() {
        Long petshopId = tenantService.currentPetshopId();
        List<Appointment> all = appointmentRepository.findAllByPetshopIdOrderByScheduledAtAsc(petshopId);

        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        BigDecimal revenue = sumInMonth(all, current);
        BigDecimal previousRevenue = sumInMonth(all, previous);

        double change = 0;
        if (previousRevenue.signum() != 0) {
            change = revenue.subtract(previousRevenue)
                    .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        } else if (revenue.signum() != 0) {
            change = 100;
        }

        long appointments = all.stream().filter(a -> inMonth(a, current)).count();
        BigDecimal averageTicket = appointments == 0
                ? ZERO
                : revenue.divide(BigDecimal.valueOf(appointments), 2, RoundingMode.HALF_UP);

        Map<Long, Long> byClient = new LinkedHashMap<>();
        for (Appointment appointment : all) {
            if (appointment.getClient() != null) {
                byClient.merge(appointment.getClient().getId(), 1L, Long::sum);
            }
        }
        long returning = byClient.values().stream().filter(count -> count >= 2).count();
        double returnRate = byClient.isEmpty() ? 0 : Math.round(returning * 100.0 / byClient.size());

        List<ReportsSummary.MonthlyRevenue> monthly = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            monthly.add(new ReportsSummary.MonthlyRevenue(
                    MONTHS[month.getMonthValue() - 1], sumInMonth(all, month)));
        }

        Map<String, BigDecimal> byService = new LinkedHashMap<>();
        for (Appointment appointment : all) {
            if (inMonth(appointment, current)) {
                byService.merge(appointment.getService(), money(appointment), BigDecimal::add);
            }
        }
        BigDecimal serviceTotal = byService.values().stream().reduce(ZERO, BigDecimal::add);
        List<ReportsSummary.ServiceShare> services = byService.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new ReportsSummary.ServiceShare(
                        entry.getKey(),
                        entry.getValue().setScale(2, RoundingMode.HALF_UP),
                        serviceTotal.signum() == 0 ? 0
                                : entry.getValue().multiply(BigDecimal.valueOf(100))
                                        .divide(serviceTotal, 0, RoundingMode.HALF_UP).intValue()))
                .toList();

        return new ReportsSummary(
                MONTHS_LONG[current.getMonthValue() - 1] + " de " + current.getYear(),
                revenue.setScale(2, RoundingMode.HALF_UP),
                previousRevenue.setScale(2, RoundingMode.HALF_UP),
                change,
                averageTicket,
                appointments,
                returnRate,
                monthly,
                services);
    }

    private BigDecimal sumInMonth(List<Appointment> appointments, YearMonth month) {
        return appointments.stream()
                .filter(appointment -> inMonth(appointment, month))
                .map(ReportsService::money)
                .reduce(ZERO, BigDecimal::add);
    }

    private static boolean inMonth(Appointment appointment, YearMonth month) {
        LocalDateTime when = appointment.getScheduledAt();
        return when != null && YearMonth.from(when).equals(month);
    }

    private static BigDecimal money(Appointment appointment) {
        return appointment.getPrice() != null ? appointment.getPrice() : ZERO;
    }
}

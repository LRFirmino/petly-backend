package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportsSummary(
        String periodLabel,
        BigDecimal revenue,
        BigDecimal previousRevenue,
        double revenueChangePct,
        BigDecimal averageTicket,
        long appointments,
        double returnRate,
        List<MonthlyRevenue> monthly,
        List<ServiceShare> services,
        List<ServiceShare> combos) {

    public record MonthlyRevenue(String month, BigDecimal revenue) {
    }

    public record ServiceShare(String name, BigDecimal revenue, int sharePct) {
    }
}

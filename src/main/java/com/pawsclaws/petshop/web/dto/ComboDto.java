package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComboDto(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        List<BigDecimal> prices,
        BigDecimal priceFrom,
        long popularity) {
}

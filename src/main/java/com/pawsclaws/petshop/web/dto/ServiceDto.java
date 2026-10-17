package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record ServiceDto(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        List<BigDecimal> prices) {
}

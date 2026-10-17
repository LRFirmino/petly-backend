package com.pawsclaws.petshop.web.dto;

import java.math.BigDecimal;

public record ComboDto(
        Long id,
        String name,
        String description,
        BigDecimal priceFrom,
        Integer popularity) {
}

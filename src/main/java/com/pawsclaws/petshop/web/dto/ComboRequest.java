package com.pawsclaws.petshop.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ComboRequest(
        @NotBlank(message = "Informe o nome do combo.")
        @Size(max = 160)
        String name,

        @Size(max = 500)
        String description,

        Integer durationMinutes,

        BigDecimal priceSmall,

        BigDecimal priceMedium,

        BigDecimal priceLarge) {
}

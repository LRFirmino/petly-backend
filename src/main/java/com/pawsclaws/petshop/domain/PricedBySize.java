package com.pawsclaws.petshop.domain;

import java.math.BigDecimal;

/**
 * Item de catálogo vendido em três faixas de porte (pequeno/médio/grande).
 * Implementado por {@link ServiceItem} e {@link Combo} para que o preço possa
 * ser resolvido de forma uniforme a partir do porte do pet.
 */
public interface PricedBySize {

    BigDecimal getPriceSmall();

    BigDecimal getPriceMedium();

    BigDecimal getPriceLarge();
}

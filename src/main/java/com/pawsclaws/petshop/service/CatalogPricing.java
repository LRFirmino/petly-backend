package com.pawsclaws.petshop.service;

import com.pawsclaws.petshop.domain.PricedBySize;
import com.pawsclaws.petshop.domain.SizeTier;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Resolução de preço compartilhada entre serviços e combos.
 *
 * <p>Usada tanto ao montar os preços de apresentação ("a partir de") quanto ao
 * precificar um agendamento a partir do porte do pet.</p>
 */
final class CatalogPricing {

    private CatalogPricing() {
    }

    /**
     * Preço do item para o porte informado, com fallback previsível quando uma
     * faixa não está cadastrada: preço do porte → médio → pequeno → grande.
     * Retorna {@code null} somente se o item não tiver nenhum preço.
     */
    static BigDecimal priceForSize(PricedBySize item, SizeTier size) {
        if (item == null) {
            return null;
        }
        BigDecimal chosen = null;
        if (size == SizeTier.SMALL) {
            chosen = item.getPriceSmall();
        } else if (size == SizeTier.MEDIUM) {
            chosen = item.getPriceMedium();
        } else if (size == SizeTier.LARGE) {
            chosen = item.getPriceLarge();
        }
        if (chosen != null) {
            return chosen;
        }
        if (item.getPriceMedium() != null) {
            return item.getPriceMedium();
        }
        if (item.getPriceSmall() != null) {
            return item.getPriceSmall();
        }
        return item.getPriceLarge();
    }

    /** Menor preço cadastrado, usado como valor "a partir de". */
    static BigDecimal minPrice(PricedBySize item) {
        if (item == null) {
            return null;
        }
        return Stream.of(item.getPriceSmall(), item.getPriceMedium(), item.getPriceLarge())
                .filter(java.util.Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }
}

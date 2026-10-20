package com.pawsclaws.petshop.domain;

/**
 * Tipo de item de catálogo que um agendamento pode referenciar.
 *
 * <p>Um agendamento aponta para {@code SERVICE} (um {@link ServiceItem}) ou
 * {@code COMBO} (um {@link Combo}) através de {@code item_type} + {@code item_id}.
 * Itens legados sem vínculo ({@code item_type} nulo) são tratados como serviço.</p>
 */
public enum CatalogItemType {
    SERVICE,
    COMBO
}

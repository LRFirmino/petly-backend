package com.pawsclaws.petshop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A standalone service offered by a petshop (Banho, Tosa, Hidratação...).
 * Tenant scoped through {@link #petshop}.
 */
@Entity
@Table(name = "services")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "petshop_id", nullable = false)
    private Petshop petshop;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "price_small", precision = 10, scale = 2)
    private BigDecimal priceSmall;

    @Column(name = "price_medium", precision = 10, scale = 2)
    private BigDecimal priceMedium;

    @Column(name = "price_large", precision = 10, scale = 2)
    private BigDecimal priceLarge;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Petshop getPetshop() {
        return petshop;
    }

    public void setPetshop(Petshop petshop) {
        this.petshop = petshop;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BigDecimal getPriceSmall() {
        return priceSmall;
    }

    public void setPriceSmall(BigDecimal priceSmall) {
        this.priceSmall = priceSmall;
    }

    public BigDecimal getPriceMedium() {
        return priceMedium;
    }

    public void setPriceMedium(BigDecimal priceMedium) {
        this.priceMedium = priceMedium;
    }

    public BigDecimal getPriceLarge() {
        return priceLarge;
    }

    public void setPriceLarge(BigDecimal priceLarge) {
        this.priceLarge = priceLarge;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServiceItem service) || id == null) {
            return false;
        }
        return id.equals(service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

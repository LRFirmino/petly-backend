package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findAllByPetshopIdOrderByNameAsc(Long petshopId);

    Optional<ServiceItem> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);
}

package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.Combo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComboRepository extends JpaRepository<Combo, Long> {

    List<Combo> findAllByPetshopIdOrderByPopularityDesc(Long petshopId);

    Optional<Combo> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);
}

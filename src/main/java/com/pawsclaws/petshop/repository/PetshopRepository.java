package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.Petshop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetshopRepository extends JpaRepository<Petshop, Long> {

    Optional<Petshop> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Petshop> findAllByPlatformFalseOrderByNameAsc();
}

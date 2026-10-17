package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Every finder is scoped by petshopId on purpose: data can never be loaded
 * across tenant boundaries.
 */
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findAllByPetshopIdOrderByNameAsc(Long petshopId);

    Optional<Client> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);

    long countByPetshopIdAndCreatedAtAfter(Long petshopId, Instant after);
}

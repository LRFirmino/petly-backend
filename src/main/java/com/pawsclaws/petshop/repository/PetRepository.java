package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByPetshopIdOrderByNameAsc(Long petshopId);

    List<Pet> findAllByPetshopIdAndClientIdOrderByNameAsc(Long petshopId, Long clientId);

    Optional<Pet> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);

    List<Pet> findAllByPetshopIdAndBirthDateBetweenOrderByBirthDateAsc(
            Long petshopId, LocalDate from, LocalDate to);
}

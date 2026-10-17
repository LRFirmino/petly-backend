package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<AppUser> findAllByPetshopIdOrderByNameAsc(Long petshopId);

    Optional<AppUser> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);
}

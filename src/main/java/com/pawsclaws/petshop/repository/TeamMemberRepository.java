package com.pawsclaws.petshop.repository;

import com.pawsclaws.petshop.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findAllByPetshopIdOrderByNameAsc(Long petshopId);

    Optional<TeamMember> findByIdAndPetshopId(Long id, Long petshopId);

    long countByPetshopId(Long petshopId);
}

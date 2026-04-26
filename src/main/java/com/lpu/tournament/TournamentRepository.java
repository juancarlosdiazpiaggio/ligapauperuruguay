package com.lpu.tournament;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    boolean existsByMeleeId(String meleeId);
    Optional<Tournament> findByMeleeId(String meleeId);
    List<Tournament> findBySeasonId(Long seasonId);
    List<Tournament> findByOrganizerId(Long organizerId);
}

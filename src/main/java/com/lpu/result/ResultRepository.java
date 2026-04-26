package com.lpu.result;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<TournamentResult, Long> {
    List<TournamentResult> findByTournamentId(Long tournamentId);
    List<TournamentResult> findByPlayerId(Long playerId);
    List<TournamentResult> findByStatusAndTournamentId(ResultStatus status, Long tournamentId);
    List<TournamentResult> findByPlayerIdAndTournamentSeasonId(Long playerId, Long seasonId);
}

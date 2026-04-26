package com.lpu.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByMeleeUsername(String meleeUsername);
    boolean existsByMeleeUsername(String meleeUsername);
}

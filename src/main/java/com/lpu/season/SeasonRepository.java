package com.lpu.season;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByActiveTrue();
    boolean existsByActiveTrue();
}

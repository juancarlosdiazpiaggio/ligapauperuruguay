package com.lpu.archetype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArchetypeRepository extends JpaRepository<Archetype, Long> {
    List<Archetype> findByActiveTrue();
}

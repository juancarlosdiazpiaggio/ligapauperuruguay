package com.lpu.archetype;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchetypeService {

    private final ArchetypeRepository archetypeRepository;

    public Archetype create(String name) {
        Archetype archetype = new Archetype();
        archetype.setName(name);
        return archetypeRepository.save(archetype);
    }

    public List<Archetype> findActive() {
        return archetypeRepository.findByActiveTrue();
    }

    public Archetype findById(Long id) {
        return archetypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Arquetipo no encontrado: " + id));
    }
}

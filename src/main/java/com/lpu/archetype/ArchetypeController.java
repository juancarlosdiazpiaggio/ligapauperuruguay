package com.lpu.archetype;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/archetypes")
@RequiredArgsConstructor
public class ArchetypeController {

    private final ArchetypeService archetypeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Archetype> create(@RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(archetypeService.create(request.name()));
    }

    @GetMapping
    public List<Archetype> getActive() {
        return archetypeService.findActive();
    }

    public record CreateRequest(String name) {}
}

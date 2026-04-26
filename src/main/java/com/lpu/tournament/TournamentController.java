package com.lpu.tournament;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Tournament> create(@RequestBody CreateRequest request) {
        Tournament t = tournamentService.create(
                request.meleeId(), request.name(), request.date(),
                request.storeName(), request.organizerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(t);
    }

    @GetMapping("/{id}")
    public Tournament getById(@PathVariable Long id) {
        return tournamentService.findById(id);
    }

    @GetMapping
    public List<Tournament> getBySeason(@RequestParam Long seasonId) {
        return tournamentService.findBySeason(seasonId);
    }

    public record CreateRequest(
            String meleeId,
            String name,
            LocalDate date,
            String storeName,
            Long organizerId
    ) {}
}

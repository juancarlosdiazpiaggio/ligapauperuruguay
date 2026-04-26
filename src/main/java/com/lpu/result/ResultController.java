package com.lpu.result;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tournaments/{tournamentId}/results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<ResultService.ImportSummary> importFromMelee(@PathVariable Long tournamentId) {
        return ResponseEntity.ok(resultService.importFromMelee(tournamentId));
    }

    @GetMapping("/unmatched")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public List<TournamentResult> getUnmatched(@PathVariable Long tournamentId) {
        return resultService.findUnmatched(tournamentId);
    }

    @PatchMapping("/{resultId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> resolveUnmatched(
            @PathVariable Long tournamentId,
            @PathVariable Long resultId,
            @RequestBody ResolveRequest request) {
        resultService.resolveUnmatched(resultId, request.playerId());
        return ResponseEntity.noContent().build();
    }

    public record ResolveRequest(Long playerId) {}
}

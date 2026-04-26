package com.lpu.result;

import com.lpu.melee.MeleeClient;
import com.lpu.melee.MeleeParticipantResult;
import com.lpu.player.PlayerRepository;
import com.lpu.tournament.Tournament;
import com.lpu.tournament.TournamentRepository;
import com.lpu.tournament.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final TournamentRepository tournamentRepository;
    private final ResultRepository resultRepository;
    private final PlayerRepository playerRepository;
    private final MeleeClient meleeClient;
    private final PointsTable pointsTable;

    @Transactional
    public ImportSummary importFromMelee(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + tournamentId));

        if (tournament.getStatus() == TournamentStatus.IMPORTED) {
            throw new IllegalStateException("El torneo ya fue importado");
        }

        List<MeleeParticipantResult> meleeResults = meleeClient.fetchResults(tournament.getMeleeId());

        int matched = 0, unmatched = 0;
        for (MeleeParticipantResult mr : meleeResults) {
            TournamentResult result = new TournamentResult();
            result.setTournament(tournament);
            result.setPosition(mr.position());
            result.setPoints(pointsTable.pointsFor(mr.position()));
            result.setMeleeUsername(mr.meleeUsername());
            result.setDecklistUrl(mr.decklistUrl() != null ? mr.decklistUrl() : "");

            var player = playerRepository.findByMeleeUsername(mr.meleeUsername());
            if (player.isPresent()) {
                result.setPlayer(player.get());
                result.setStatus(ResultStatus.MATCHED);
                matched++;
            } else {
                result.setStatus(ResultStatus.UNMATCHED);
                unmatched++;
            }
            resultRepository.save(result);
        }

        tournament.setStatus(TournamentStatus.IMPORTED);
        tournamentRepository.save(tournament);

        return new ImportSummary(matched, unmatched);
    }

    @Transactional
    public void resolveUnmatched(Long resultId, Long playerId) {
        TournamentResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Resultado no encontrado: " + resultId));
        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado: " + playerId));
        result.setPlayer(player);
        result.setStatus(ResultStatus.MATCHED);
        resultRepository.save(result);
    }

    public List<TournamentResult> findUnmatched(Long tournamentId) {
        return resultRepository.findByStatusAndTournamentId(ResultStatus.UNMATCHED, tournamentId);
    }

    public record ImportSummary(int matched, int unmatched) {}
}

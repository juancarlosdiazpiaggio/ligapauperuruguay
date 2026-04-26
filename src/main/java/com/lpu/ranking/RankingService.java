package com.lpu.ranking;

import com.lpu.player.Player;
import com.lpu.player.PlayerRepository;
import com.lpu.result.ResultRepository;
import com.lpu.result.ResultStatus;
import com.lpu.result.TournamentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final PlayerRepository playerRepository;
    private final ResultRepository resultRepository;

    @Value("${lpu.ranking.tournaments-to-count}")
    private int tournamentsToCount;

    public List<PlayerRanking> computeRanking(Long seasonId) {
        List<Player> players = playerRepository.findAll();

        List<PlayerRanking> ranking = players.stream()
                .map(player -> computeForPlayer(player, seasonId))
                .filter(pr -> pr.tournamentsPlayed() > 0)
                .sorted(
                    Comparator.comparingInt(PlayerRanking::totalPoints).reversed()
                        .thenComparingInt(PlayerRanking::tournamentsPlayed).reversed()
                )
                .toList();

        AtomicInteger position = new AtomicInteger(1);
        return ranking.stream()
                .map(pr -> new PlayerRanking(
                        position.getAndIncrement(),
                        pr.playerId(),
                        pr.displayName(),
                        pr.totalPoints(),
                        pr.tournamentsPlayed(),
                        pr.tournamentsCountedForRanking()))
                .toList();
    }

    private PlayerRanking computeForPlayer(Player player, Long seasonId) {
        List<TournamentResult> results = resultRepository
                .findByPlayerIdAndTournamentSeasonId(player.getId(), seasonId)
                .stream()
                .filter(r -> r.getStatus() == ResultStatus.MATCHED)
                .sorted(Comparator.comparingInt(TournamentResult::getPoints).reversed())
                .toList();

        List<TournamentResult> best = results.stream()
                .limit(tournamentsToCount)
                .toList();

        int totalPoints = best.stream().mapToInt(TournamentResult::getPoints).sum();

        return new PlayerRanking(0, player.getId(), player.getDisplayName(),
                totalPoints, results.size(), best.size());
    }
}

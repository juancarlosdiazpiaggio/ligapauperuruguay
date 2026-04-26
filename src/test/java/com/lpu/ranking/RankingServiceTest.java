package com.lpu.ranking;

import com.lpu.player.Player;
import com.lpu.player.PlayerRepository;
import com.lpu.result.ResultRepository;
import com.lpu.result.ResultStatus;
import com.lpu.result.TournamentResult;
import com.lpu.season.Season;
import com.lpu.tournament.Tournament;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock PlayerRepository playerRepository;
    @Mock ResultRepository resultRepository;
    @InjectMocks RankingService rankingService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(rankingService, "tournamentsToCount", 10);
    }

    @Test
    void ranking_toma_los_mejores_10_torneos() {
        Player player = player(1L, "TestPlayer");
        when(playerRepository.findAll()).thenReturn(List.of(player));

        // 12 resultados: puntos 20,15,15,10,10,10,6,6,6,6,3,3
        List<TournamentResult> results = List.of(
                result(player, 20), result(player, 15), result(player, 15),
                result(player, 10), result(player, 10), result(player, 10),
                result(player, 6),  result(player, 6),  result(player, 6),
                result(player, 6),  result(player, 3),  result(player, 3)
        );
        when(resultRepository.findByPlayerIdAndTournamentSeasonId(1L, 1L)).thenReturn(results);

        List<PlayerRanking> ranking = rankingService.computeRanking(1L);

        assertThat(ranking).hasSize(1);
        PlayerRanking pr = ranking.get(0);
        // mejores 10: 20+15+15+10+10+10+6+6+6+6 = 104 (descarta los dos 3)
        assertThat(pr.totalPoints()).isEqualTo(104);
        assertThat(pr.tournamentsPlayed()).isEqualTo(12);
        assertThat(pr.tournamentsCountedForRanking()).isEqualTo(10);
    }

    @Test
    void ranking_con_menos_de_10_torneos_cuenta_todos() {
        Player player = player(1L, "TestPlayer");
        when(playerRepository.findAll()).thenReturn(List.of(player));

        List<TournamentResult> results = List.of(
                result(player, 20), result(player, 15), result(player, 10)
        );
        when(resultRepository.findByPlayerIdAndTournamentSeasonId(1L, 1L)).thenReturn(results);

        List<PlayerRanking> ranking = rankingService.computeRanking(1L);

        assertThat(ranking.get(0).totalPoints()).isEqualTo(45);
        assertThat(ranking.get(0).tournamentsCountedForRanking()).isEqualTo(3);
    }

    @Test
    void ranking_ordena_por_puntos_descendente() {
        Player p1 = player(1L, "Mejor");
        Player p2 = player(2L, "Peor");
        when(playerRepository.findAll()).thenReturn(List.of(p2, p1));

        when(resultRepository.findByPlayerIdAndTournamentSeasonId(1L, 1L))
                .thenReturn(List.of(result(p1, 20)));
        when(resultRepository.findByPlayerIdAndTournamentSeasonId(2L, 1L))
                .thenReturn(List.of(result(p2, 10)));

        List<PlayerRanking> ranking = rankingService.computeRanking(1L);

        assertThat(ranking.get(0).displayName()).isEqualTo("Mejor");
        assertThat(ranking.get(0).position()).isEqualTo(1);
        assertThat(ranking.get(1).displayName()).isEqualTo("Peor");
        assertThat(ranking.get(1).position()).isEqualTo(2);
    }

    @Test
    void ranking_excluye_resultados_unmatched() {
        Player player = player(1L, "TestPlayer");
        when(playerRepository.findAll()).thenReturn(List.of(player));

        TournamentResult matched = result(player, 20);
        TournamentResult unmatched = result(player, 15);
        unmatched.setStatus(ResultStatus.UNMATCHED);
        unmatched.setPlayer(null);

        when(resultRepository.findByPlayerIdAndTournamentSeasonId(1L, 1L))
                .thenReturn(List.of(matched, unmatched));

        List<PlayerRanking> ranking = rankingService.computeRanking(1L);

        assertThat(ranking.get(0).totalPoints()).isEqualTo(20);
        assertThat(ranking.get(0).tournamentsPlayed()).isEqualTo(1);
    }

    // helpers

    private Player player(Long id, String name) {
        Player p = new Player();
        p.setId(id);
        p.setDisplayName(name);
        p.setMeleeUsername(name.toLowerCase());
        return p;
    }

    private TournamentResult result(Player player, int points) {
        Season season = new Season();
        season.setId(1L);

        Tournament tournament = new Tournament();
        tournament.setId((long) (Math.random() * 1000));
        tournament.setSeason(season);

        TournamentResult r = new TournamentResult();
        r.setPlayer(player);
        r.setTournament(tournament);
        r.setPoints(points);
        r.setPosition(1);
        r.setMeleeUsername(player.getMeleeUsername());
        r.setDecklistUrl("");
        r.setStatus(ResultStatus.MATCHED);
        return r;
    }
}

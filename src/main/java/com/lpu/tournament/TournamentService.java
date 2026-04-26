package com.lpu.tournament;

import com.lpu.auth.AppUser;
import com.lpu.auth.AppUserRepository;
import com.lpu.season.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final SeasonService seasonService;
    private final AppUserRepository userRepository;

    public Tournament create(String meleeId, String name, LocalDate date, String storeName, Long organizerId) {
        if (tournamentRepository.existsByMeleeId(meleeId)) {
            throw new IllegalArgumentException("Ya existe un torneo con ese ID de Melee");
        }
        AppUser organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        Tournament tournament = new Tournament();
        tournament.setMeleeId(meleeId);
        tournament.setName(name);
        tournament.setDate(date);
        tournament.setStoreName(storeName);
        tournament.setSeason(seasonService.getActive());
        tournament.setOrganizer(organizer);
        return tournamentRepository.save(tournament);
    }

    public Tournament findById(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado: " + id));
    }

    public List<Tournament> findBySeason(Long seasonId) {
        return tournamentRepository.findBySeasonId(seasonId);
    }
}

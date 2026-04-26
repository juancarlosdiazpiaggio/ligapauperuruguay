package com.lpu.season;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public Season create(int year) {
        if (seasonRepository.existsByActiveTrue()) {
            throw new IllegalStateException("Ya existe una temporada activa");
        }
        Season season = new Season();
        season.setYear(year);
        season.setStartDate(LocalDate.of(year, 1, 1));
        season.setEndDate(LocalDate.of(year, 12, 31));
        season.setActive(true);
        return seasonRepository.save(season);
    }

    public Season getActive() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No hay temporada activa"));
    }
}

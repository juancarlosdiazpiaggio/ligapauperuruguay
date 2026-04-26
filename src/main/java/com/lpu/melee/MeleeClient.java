package com.lpu.melee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeleeClient {

    private final WebClient webClient = WebClient.create("https://melee.gg");

    /**
     * Obtiene los resultados de un torneo de Melee dado su ID.
     * La implementacion completa depende de la API/estructura de Melee.
     */
    @SuppressWarnings("unchecked")
    public List<MeleeParticipantResult> fetchResults(String meleeId) {
        List<Map<String, Object>> raw = webClient.get()
                .uri("/api/v1/tournament/{id}/standings", meleeId)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(entry -> (Map<String, Object>) entry)
                .collectList()
                .block();

        return raw.stream()
                .map(entry -> new MeleeParticipantResult(
                        (String) entry.get("username"),
                        (Integer) entry.get("standing"),
                        (String) entry.getOrDefault("decklistUrl", null)
                ))
                .toList();
    }
}

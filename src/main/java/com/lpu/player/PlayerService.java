package com.lpu.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player register(String displayName, String meleeUsername) {
        if (playerRepository.existsByMeleeUsername(meleeUsername)) {
            throw new IllegalArgumentException("El username de Melee ya esta registrado");
        }
        Player player = new Player();
        player.setDisplayName(displayName);
        player.setMeleeUsername(meleeUsername);
        return playerRepository.save(player);
    }

    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id));
    }

    public List<Player> findAll() {
        return playerRepository.findAll();
    }
}

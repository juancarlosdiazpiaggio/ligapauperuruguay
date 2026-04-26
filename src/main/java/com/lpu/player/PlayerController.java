package com.lpu.player;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<Player> register(@Valid @RequestBody RegisterRequest request) {
        Player player = playerService.register(request.displayName(), request.meleeUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(player);
    }

    @GetMapping("/{id}")
    public Player getById(@PathVariable Long id) {
        return playerService.findById(id);
    }

    @GetMapping
    public List<Player> getAll() {
        return playerService.findAll();
    }

    public record RegisterRequest(
            @NotBlank String displayName,
            @NotBlank String meleeUsername
    ) {}
}

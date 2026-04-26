package com.lpu.result;

import com.lpu.archetype.Archetype;
import com.lpu.player.Player;
import com.lpu.tournament.Tournament;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_results")
@Getter @Setter @NoArgsConstructor
public class TournamentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archetype_id")
    private Archetype archetype;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int points;

    // Username original de Melee, para resolver casos UNMATCHED
    @Column(nullable = false)
    private String meleeUsername;

    @Column(nullable = false)
    private String decklistUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultStatus status;
}

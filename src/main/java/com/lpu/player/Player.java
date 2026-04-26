package com.lpu.player;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter @Setter @NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String displayName;

    @Column(unique = true, nullable = false)
    private String meleeUsername;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

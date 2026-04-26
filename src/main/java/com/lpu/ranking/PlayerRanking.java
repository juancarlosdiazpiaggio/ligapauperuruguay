package com.lpu.ranking;

public record PlayerRanking(
        int position,
        Long playerId,
        String displayName,
        int totalPoints,
        int tournamentsPlayed,
        int tournamentsCountedForRanking
) {}

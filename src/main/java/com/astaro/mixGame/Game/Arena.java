package com.astaro.mixGame.Game;

import org.bukkit.Location;

public record Arena(
        String arenaName,
        Location arenaLoc1,
        Location arenaLoc2,
        Location lobbyLoc,
        Location spawnLocation,
        Location endLoc,
        int minPlayers,
        int maxPlayers,
        int colorLimit,
        int sectionSize,
        int level,
        String floorMaterial,
        boolean isCollision) {}


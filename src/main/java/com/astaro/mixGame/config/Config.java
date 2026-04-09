package com.astaro.mixGame.config;

import org.bukkit.Material;

import java.util.List;

public record Config(
        String configVersion,
        String lang,
        BossBarSettings bossBar,
        int lobbyCountdown,
        int gameCountdown,
        DatabaseSettings db,
        List<String> whitelistedCommands,
        boolean expBarEnabled,
        int pointsToLevel,
        boolean saveInventory,
        boolean saveExp,
        boolean scoreboardEnabled,
        boolean tabListEnabled,
        boolean titleTextEnabled,
        RewardsSettings rewards,
        Material leaveItem,
        SongSettings songs
) {
    public record BossBarSettings(boolean enabled, String color) {
    }

    public record DatabaseSettings(
            String type, String host, String database,
            String username, String password, int port, String table
    ) {
    }

    public record RewardsSettings(
            boolean enabled,
            List<String> perRound,
            List<String> perGame,
            List<String> winner
    ) {
    }

    public record SongSettings(boolean lobby, boolean arena) {
    }
}


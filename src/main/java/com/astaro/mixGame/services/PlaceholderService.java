package com.astaro.mixGame.services;

import com.astaro.mixGame.Game.ArenaController;
import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.data.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholderService extends PlaceholderExpansion {

    private final MixGame plugin;
    private final Map<String, List<String>> topCache = new ConcurrentHashMap<>();
    private final Map<String, PlayerStats> playerCache = new ConcurrentHashMap<>();
    private long lastTopUpdate = 0;

    public PlaceholderService(MixGame plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Astaro";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mixgame";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // --- ЛОГИКА ТОПОВ (%mixgame_top_points_1_name%) ---
        if (params.startsWith("top_")) {
            updateTopCache();

            String[] parts = params.split("_");
            // Ожидаемая структура: [top, тип(points), инфо(name/value), ранг(1-10)]
            if (parts.length < 4) return "---";

            String type = parts[1].toLowerCase();   // points
            String infoType = parts[2].toLowerCase(); // name или value
            String rankRaw = parts[3];             // 1

            int rank;
            try {
                rank = Integer.parseInt(rankRaw) - 1;
            } catch (NumberFormatException e) {
                return "ERR_RANK";
            }

            List<String> topList = topCache.get(type);
            if (topList == null || rank < 0 || rank >= topList.size()) {
                return "---";
            }

            String entry = topList.get(rank);
            String[] data = entry.split(":");

            if (data.length < 2) return "---";

            return infoType.equalsIgnoreCase("name") ? data[0] : data[1];
        }

        String name = player.getName();
        if (name == null) return "0";

        if (!playerCache.containsKey(name)) {
            plugin.getDatabase().getUserStats(name).thenAccept(opt ->
                    opt.ifPresent(s -> playerCache.put(name, s))
            );
            return "...";
        }

        PlayerStats stats = playerCache.get(name);

        return switch (params.toLowerCase()) {
            case "points" -> String.valueOf(stats.points());
            case "won" -> String.valueOf(stats.won());
            case "lost" -> String.valueOf(stats.lost());
            case "played" -> String.valueOf(stats.getPlayedGames());
            case "wlr" -> String.format("%.2f", stats.getWLR());
            case "winrate" -> {
                int total = stats.getPlayedGames();
                if (total == 0) yield "0%";
                yield String.format("%.1f%%", (stats.won() * 100.0) / total);
            }
            case "level" -> String.valueOf(stats.currentLevel());
            case "points_to_next" -> {
                int currentLvl = stats.currentLevel();
                int nextLvlPoints = stats.getPointsForLevel(++currentLvl);
                yield String.valueOf(nextLvlPoints - stats.points());
            }
            default -> null;
        };
    }

    private void updateTopCache() {
        long now = System.currentTimeMillis();
        if (now - lastTopUpdate < 300000) return; // Обновляем раз в 5 минут
        lastTopUpdate = now;

        String[] columns = {"points", "won", "lost"};
        for (String col : columns) {
            plugin.getDatabase().getTop(col, 10).thenAccept(list -> topCache.put(col, list));
        }
        }

    public void invalidateCache(String username) {
        playerCache.remove(username);
    }
}


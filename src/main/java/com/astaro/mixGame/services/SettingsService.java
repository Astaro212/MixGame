package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.config.Config;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;

public class SettingsService {
    private final MixGame plugin;
    private Config current;

    public SettingsService(MixGame plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        var bossbar = new Config.BossBarSettings(
                c.getBoolean("bossbar.enabled"),
                c.getString("bossbar.color", "PINK")
        );

        var db = new Config.DatabaseSettings(
                c.getString("database.type", "SQLite"),
                c.getString("database.host"),
                c.getString("database.database"),
                c.getString("database.username"),
                c.getString("database.password"),
                c.getInt("database.port"),
                c.getString("database.table")
        );

        var rewards = new Config.RewardsSettings(
                c.getBoolean("rewards.enabled"),
                c.getStringList("rewards.per-round-rewards"),
                c.getStringList("rewards.per-game-rewards"),
                c.getStringList("rewards.winner-rewards")
        );

        var songs = new Config.SongSettings(
                c.getBoolean("songs.lobby"),
                c.getBoolean("songs.arena")
        );

        this.current = new Config(
                c.getString("config-version", "1.0.0"),
                c.getString("lang", "ru"),
                bossbar,
                c.getInt("lobby-countdown", 30),
                c.getInt("game-countdown", 10),
                db,
                c.getStringList("WhitelistedCommands"),
                c.getBoolean("expbar"),
                c.getInt("points-to-level"),
                c.getBoolean("save-inventory"),
                c.getBoolean("save-exp"),
                c.getBoolean("scoreboard.enabled"),
                c.getBoolean("tablist"),
                c.getBoolean("titlettext"),
                rewards,
                Material.matchMaterial(c.getString("leave.item", "WHITE_BED")),
                songs
        );
        if(!Objects.equals(current.configVersion(), "1.0.0")){
            plugin.saveDefaultConfig();
        }
    }

    public Config get() {
        return current;
    }
}

package com.astaro.mixGame.Game;

import com.astaro.mixGame.MixGame;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final MixGame plugin;
    private final Map<String, ArenaController> arenas = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToArena = new ConcurrentHashMap<>();
    private final File arenaFile;
    private FileConfiguration arenaConfig;

    public ArenaManager(MixGame plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        loadArenas();
        loadAllArenas();
    }

    public void loadArenas() {
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);
    }

    public void registerArena(ArenaController controller) {
        arenas.put(controller.getSettings().arenaName().toLowerCase(), controller);
    }


    public ArenaController getArenaByPlayer(Player player) {
        String arenaName = playerToArena.get(player.getUniqueId());
        return arenaName != null ? arenas.get(arenaName.toLowerCase()) : null;
    }


    public void joinPlayer(Player player, String arenaName) {
        ArenaController controller = arenas.get(arenaName.toLowerCase());
        if (controller == null) {
            plugin.getChatService().sendMessage(player, "ErrorMessages.arenaNotFound");
            return;
        }

        controller.join(player);
        playerToArena.put(player.getUniqueId(), arenaName.toLowerCase());
    }


    public void leavePlayer(Player player) {
        String arenaName = playerToArena.remove(player.getUniqueId());
        if (arenaName != null) {
            ArenaController controller = arenas.get(arenaName);
            if (controller != null) {
                controller.leave(player);
            }
        }
    }

    public Collection<ArenaController> getAllArenas() {
        return arenas.values();
    }

    public void loadAllArenas() {
        if (!arenaConfig.contains("arenas")) return;

        for (String name : arenaConfig.getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + name;

            Arena settings = new Arena(
                    name,
                    arenaConfig.getLocation(path + ".loc1"),
                    arenaConfig.getLocation(path + ".loc2"),
                    arenaConfig.getLocation(path + ".lobby"),
                    arenaConfig.getLocation(path + ".spawn"),
                    arenaConfig.getLocation(path + ".end"),
                    arenaConfig.getInt(path + ".minPlayers"),
                    arenaConfig.getInt(path + ".maxPlayers"),
                    5,
                    arenaConfig.getInt(path + ".sectionSize"),
                    1,
                    arenaConfig.getString(path + ".floorMaterial"),
                    true
            );

            FloorManager fm = new FloorManager(settings.arenaLoc1(), settings.arenaLoc2(), settings.sectionSize());
            arenas.put(name.toLowerCase(), new ArenaController(plugin, settings, fm));
        }
    }

    public void saveArena(Arena arena) {
        String path = "arenas." + arena.arenaName();
        arenaConfig.set(path + ".loc1", arena.arenaLoc1());
        arenaConfig.set(path + ".loc2", arena.arenaLoc2());
        arenaConfig.set(path + ".lobby", arena.lobbyLoc());
        arenaConfig.set(path + ".spawn", arena.spawnLocation());
        arenaConfig.set(path + ".end", arena.endLoc());
        arenaConfig.set(path + ".minPlayers", arena.minPlayers());
        arenaConfig.set(path + ".maxPlayers", arena.maxPlayers());
        arenaConfig.set(path + ".sectionSize", arena.sectionSize());
        arenaConfig.set(path + ".floorMaterial", arena.floorMaterial());

        try {
            arenaConfig.save(arenaFile);
            String lowerName = arena.arenaName().toLowerCase();
            if (arenas.containsKey(lowerName)) {
                deleteArena(arena.arenaName());
            }
            FloorManager fm = new FloorManager(arena.arenaLoc1(), arena.arenaLoc2(), arena.sectionSize());
            ArenaController controller = new ArenaController(plugin, arena, fm);
            registerArena(controller);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить арену " + arena.arenaName());
        }
    }

    public ArenaController getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    public void deleteArena(String name) {
        String lowerName = name.toLowerCase();
        ArenaController controller = arenas.remove(lowerName);

        if (controller != null) {
            controller.getBukkitPlayers().forEach(controller::leave);
        }

        arenaConfig.set("arenas." + name, null);
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


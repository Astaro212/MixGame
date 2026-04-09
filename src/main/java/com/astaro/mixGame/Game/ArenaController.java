package com.astaro.mixGame.Game;

import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.data.PlayerSession;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaController {
    private final Arena settings;
    private final MixGame plugin;
    private final FloorManager floorManager;
    private final ArenaHUD arenaHUD;

    private final Set<UUID> players = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<UUID> spectators = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private List<Material> mixed;

    private RadioSongPlayer musicPlayer;
    private ArenaStatus status = ArenaStatus.WAITING;
    private int currentRound = 0;
    private int timer;


    private Material currentMaterial;
    private GameTask gameTask;

    public ArenaController(MixGame plugin, Arena settings, FloorManager floorManager) {
        this.plugin = plugin;
        this.settings = settings;
        this.floorManager = floorManager;
        this.arenaHUD = new ArenaHUD(this);
        this.timer = plugin.getSettings().lobbyCountdown();
        this.mixed = plugin.getFloorService().getMixedList();
    }

    public void startRound() {
        this.status = ArenaStatus.STARTING;
        this.currentRound++;
    }

    public void stopGame(Player winner) {
        this.status = ArenaStatus.ENDING;
        if (gameTask != null) gameTask.cancel();
        stopMusic();

        if (winner != null && plugin.getSettings().rewards().enabled()) {
            plugin.getChatService().broadcast("PlayerMessages.winner", "%player%", winner.getName());
            plugin.getNotificationService().sendTitle(winner, "Titles.winner", "%player%", winner.getName());
            giveRewards(winner, plugin.getSettings().rewards().winner());
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Set<UUID> allToLeave = new HashSet<>(players);
            allToLeave.addAll(spectators);
            for (UUID uuid : allToLeave) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) leave(p);
            }
            resetArena();
        }, 100L);
    }

    private void resetArena() {
        this.status = ArenaStatus.WAITING;
        this.currentRound = 0;
        this.players.clear();
        this.spectators.clear();
        this.sessions.clear();
        floorManager.reset();
    }


    public void join(Player player) {
        UUID uuid = player.getUniqueId();

        if (status == ArenaStatus.PLAYING || status == ArenaStatus.ENDING || status == ArenaStatus.EDITING) {
            plugin.getChatService().sendMessage(player, "ErrorMessages.arenaBusy");
            return;
        }

        if (players.size() >= settings.maxPlayers()) {
            plugin.getChatService().sendMessage(player, "ErrorMessages.arenaFull");
            return;
        }

        sessions.put(uuid, PlayerSession.create(player));
        players.add(uuid);

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setItem(8, plugin.getItemService().getLeaveItem());
        player.teleportAsync(settings.lobbyLoc());

        plugin.getChatService().broadcast("PlayerMessages.playerJoin", "%player%", player.getName());
        startMusic(true);
        if (status == ArenaStatus.WAITING && players.size() >= settings.minPlayers()) {
            this.status = ArenaStatus.STARTING;
            this.timer = plugin.getSettings().lobbyCountdown();
            this.gameTask = new GameTask(plugin, this);
            this.gameTask.runTaskTimer(plugin, 0L, 20L);
        }
    }


    public void startGame() {
        this.status = ArenaStatus.PLAYING;
        this.currentRound = 1;

        Location spawn = settings.spawnLocation();

        if (spawn == null) {
            MixGame.loggerService.error("ОШИБКА: spawnLocation не установлен в настройках арены!");
            return;
        }

        for (UUID uuid : players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.teleportAsync(spawn).thenAccept(success -> {
                    if (!success) {
                        Bukkit.getScheduler().runTask(plugin, () -> p.teleport(spawn));
                    }
                });
            }
        }

        startMusic(false);
    }

    public void updateProgress(GameTask.GamePhase phase, int time) {
        String msg;
        float progress = 1.0f;
        BossBar.Color color = BossBar.Color.WHITE;
        switch (phase) {
            case LOBBY -> {
                msg = "Старт через: <white>" + time + "с.";
                progress = (float) time / plugin.getSettings().lobbyCountdown();
                color =  BossBar.Color.GREEN;
            }
            case SHOWING_COLOR -> msg = "Цвет: " + time;
            default -> msg = "Приготовьтесь!";
        }
        arenaHUD.updateBossBar(msg, progress, color);
        arenaHUD.updateSidebar();
        getBukkitPlayers().forEach(p -> plugin.getChatService().sendActionBar(p, msg));
    }

    public void setSpectators(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
        spectators.add(uuid);
        player.setGameMode(GameMode.SPECTATOR);
        player.teleportAsync(settings.spawnLocation());
    }

    public void startMusic(boolean isLobby) {
        stopMusic();
        this.musicPlayer = MixGame.instance.getMusicService().createArenaPlayer(isLobby);

        if (musicPlayer != null) {
            getBukkitPlayers().forEach(musicPlayer::addPlayer);
            musicPlayer.setPlaying(true);
        }
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.setPlaying(false);
            musicPlayer = null;
        }
    }

    public void leave(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerSession session = sessions.get(uuid);
        if (session != null) {
            session.restore(player);
            sessions.remove(uuid);
        }

        players.remove(uuid);
        spectators.remove(uuid);
        if (musicPlayer != null) musicPlayer.removePlayer(player);
        arenaHUD.clearHUD(player);

        if (status == ArenaStatus.PLAYING) {
            List<Player> alive = getBukkitPlayers();
            if (alive.size() <= 1) {
                stopGame(alive.isEmpty() ? null : alive.get(0));
            }
        }


        if (players.isEmpty() && spectators.isEmpty() && status != ArenaStatus.WAITING) {
            resetArena();
        }
    }


    public boolean isInside(Location loc) {
        if (loc == null || settings.arenaLoc1() == null || settings.arenaLoc2() == null) return false;
        if (!loc.getWorld().equals(settings.arenaLoc1().getWorld())) return false;

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        double minX = Math.min(settings.arenaLoc1().getX(), settings.arenaLoc2().getX()) - 1.5;
        double maxX = Math.max(settings.arenaLoc1().getX(), settings.arenaLoc2().getX()) + 1.5;
        double minY = Math.min(settings.arenaLoc1().getY(), settings.arenaLoc2().getY())- 0.5;
        double maxY = Math.max(settings.arenaLoc1().getY(), settings.arenaLoc2().getY()) + 10.0;
        double minZ = Math.min(settings.arenaLoc1().getZ(), settings.arenaLoc2().getZ()) - 1.5;
        double maxZ = Math.max(settings.arenaLoc1().getZ(), settings.arenaLoc2().getZ()) + 1.5;

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }


    public List<Player> getBukkitPlayers() {
        return players.stream().map(Bukkit::getPlayer)
                .filter(Objects::nonNull).toList();
    }

    public void eliminatePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!players.contains(uuid)) return;

       plugin.getNotificationService().sendTitle(player, "Titles.loser", "%player%", player.getName());

        setSpectators(player);

        List<Player> alive = getBukkitPlayers();
        if (alive.size() <= 1) {
            Player winner = alive.isEmpty() ? null : alive.get(0);
            stopGame(winner);
        }
    }

    private void giveRewards(Player winner, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;

        for (String cmd : commands) {
            String finalCmd = cmd.replace("/", "").replace("%player%", winner.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
    }


    public Arena getSettings() {
        return settings;
    }

    public ArenaStatus getStatus() {
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void nextRound() {
        this.currentRound++;
    }

    public Material getCurrentMaterial() {
        return currentMaterial;
    }

    public void setCurrentMaterial(Material m) {
        this.currentMaterial = m;
    }

    public FloorManager getFloorManager() {
        return floorManager;
    }

    public List<Material> getMixedList() {
        return this.mixed;
    }

    public ArenaHUD  getArenaHUD() {
        return arenaHUD;
    }

    public enum ArenaStatus {
        WAITING, STARTING, PLAYING, ENDING, EDITING
    }
}



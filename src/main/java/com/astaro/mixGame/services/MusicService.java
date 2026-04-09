package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MusicService {
    private final List<Song> lobbySongs = new ArrayList<>();
    private final List<Song> arenaSongs = new ArrayList<>();

    private final MixGame plugin;

    public MusicService(MixGame plugin) {
        this.plugin = plugin;
        saveDefaultSongs();
    }

    public void loadSongs() {
        lobbySongs.clear();
        arenaSongs.clear();

        loadFromDir(new File(plugin.getDataFolder(), "Songs/Lobby"), lobbySongs);
        loadFromDir(new File(plugin.getDataFolder(), "Songs/Arena"), arenaSongs);

        plugin.getLogger().info("Загружено песен: Лобби - " + lobbySongs.size() + ", Арена - " + arenaSongs.size());
    }

    private void loadFromDir(File dir, List<Song> list) {
        if (!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".nbs"));
        if (files == null) return;

        for (File file : files) {
            Song song = NBSDecoder.parse(file);
            if (song != null) list.add(song);
        }
    }

    public RadioSongPlayer createArenaPlayer(boolean isLobby) {
        List<Song> pool = isLobby ? lobbySongs : arenaSongs;
        if (pool.isEmpty()) return null;

        Song randomSong = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        RadioSongPlayer rsp = new RadioSongPlayer(randomSong);
        rsp.setAutoDestroy(true);
        return rsp;
    }

    public void saveDefaultSongs() {
        String[] lobbyDefaults = {"Axel F - Beverly Hills Cop.nbs", "Rock and Roll All Night.nbs"};
        String[] arenaDefaults = {"Levels.nbs", "Zelda Theme tune.nbs"};

        saveResources("Songs/Lobby", lobbyDefaults);
        saveResources("Songs/Arena", arenaDefaults);
    }

    private void saveResources(String path, String[] files) {
        File dir = new File(plugin.getDataFolder(), path);
        if (!dir.exists()) dir.mkdirs();

        for (String fileName : files) {
            File out = new File(dir, fileName);
            if (!out.exists()) {
                plugin.saveResource(path + "/" + fileName, false);
            }
        }
    }



}



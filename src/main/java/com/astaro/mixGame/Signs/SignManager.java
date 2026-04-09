package com.astaro.mixGame.Signs;


import org.bukkit.Location;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignManager {
    private final Map<Location, String> activeSigns = new ConcurrentHashMap<>();

    public void registerSign(Location loc, String arenaName) {
        activeSigns.put(loc, arenaName.toLowerCase());
    }

    public String getArenaBySign(Location loc) {
        return activeSigns.get(loc);
    }

    public void unregisterSign(Location loc) {
        activeSigns.remove(loc);
    }
}


package com.astaro.mixGame.data;

import com.astaro.mixGame.Game.Arena;
import org.bukkit.Location;

import java.util.UUID;

public class SetupSession {
    private final UUID adminId;
    private final String name;
    private String floorMaterial = "MIX";
    private int minPlayers = 2;
    private int maxPlayers = 16;
    private int sectionSize = 3;
    private Location loc1, loc2, lobby, spawn, end;

    public SetupSession(UUID adminId, String name) {
        this.adminId = adminId;
        this.name = name;
    }

    public SetupSession setLoc1(Location l) {
        this.loc1 = l;
        return this;
    }

    public SetupSession setLoc2(Location l) {
        this.loc2 = l;
        return this;
    }

    public Location getLoc1() {
        return this.loc1;
    }

    public Location getLoc2() {
        return this.loc2;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public SetupSession setLobby(Location l) {
        this.lobby = l;
        return this;
    }

    public SetupSession setSpawn(Location l) {
        this.spawn = l;
        return this;
    }

    public SetupSession setEnd(Location l) {
        this.end = l;
        return this;
    }

    public SetupSession setMinPlayers(int m) {
        this.minPlayers = m;
        return this;
    }

    public SetupSession setMaxPlayers(int m) {
        this.maxPlayers = m;
        return this;
    }

    public SetupSession setSectionSize(int s) {
        this.sectionSize = s;
        return this;
    }

    public SetupSession setFloorMaterial(String m) {
        this.floorMaterial = m;
        return this;
    }


    public Arena build() {
        if (loc1 == null || loc2 == null || lobby == null || spawn == null || end == null) {
            return null;
        }

        return new Arena(
                name, loc1, loc2, lobby, spawn, end,
                minPlayers, maxPlayers, 5, sectionSize, 1, floorMaterial, true
        );
    }
}

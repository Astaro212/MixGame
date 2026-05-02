package com.astaro.mixGame.data;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record PlayerSession(
        UUID uuid,
        ItemStack[] savedInventory,
        int savedLevel,
        float savedExp,
        GameMode currentGameMode,
        Location returnLocation,
        int roundsSurvived,
        boolean isSpectator
) {

    public static PlayerSession create(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] contentsCopy = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) contentsCopy[i] = contents[i].clone();
        }
        return new PlayerSession(
                player.getUniqueId(),
                contentsCopy,
                player.getLevel(),
                player.getExp(),
                player.getGameMode(),
                player.getLocation(),
                0,
                false
        );
    }

    public void restore(Player player) {
        if (player == null || !player.getUniqueId().equals(uuid)) return;

        player.getInventory().setContents(savedInventory);
        player.setLevel(savedLevel);
        player.setExp(savedExp);
        player.setGameMode(GameMode.SURVIVAL);
        player.teleportAsync(returnLocation);
    }
}

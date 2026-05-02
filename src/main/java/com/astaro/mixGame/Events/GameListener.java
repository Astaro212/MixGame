package com.astaro.mixGame.Events;

import com.astaro.mixGame.Game.ArenaController;
import com.astaro.mixGame.MixGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameListener implements Listener {

    private final MixGame plugin;
    private final Map<UUID, Long> lastPacketTime = new HashMap<>();

    public GameListener(MixGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        ArenaController arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null || arena.getStatus() != ArenaController.ArenaStatus.PLAYING) return;

        e.setCancelled(true);

        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            arena.eliminatePlayer(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ArenaController arena = plugin.getArenaManager().getArenaByPlayer(event.getPlayer());
        if (arena != null && arena.isInside(event.getBlock().getLocation())) {
            if (arena.getStatus() != ArenaController.ArenaStatus.EDITING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getFrom().getBlockY() == e.getTo().getBlockY()) return;

        Player player = e.getPlayer();
        ArenaController arena = plugin.getArenaManager().getArenaByPlayer(player);

        if (arena != null && arena.getStatus() == ArenaController.ArenaStatus.PLAYING) {
            if (e.getTo().getY() < arena.getSettings().arenaLoc1().getY() - 1 || !arena.isInside(e.getTo())) {
                arena.eliminatePlayer(player);
            }

            if (arena.getBukkitPlayers().contains(player)) {
                if (arena.getStatus() == ArenaController.ArenaStatus.PLAYING
                        && !arena.isInside(e.getTo())) {

                    e.setCancelled(true);
                    player.sendMessage("Вы не можете покинуть арену!");
                }
            }
        }

    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p && plugin.getArenaManager().getArenaByPlayer(p) != null) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        plugin.getArenaManager().leavePlayer(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null || item.getType() == Material.AIR) return;

        if (item.isSimilar(plugin.getItemService().getLeaveItem())) {
            e.setCancelled(true);
            plugin.getArenaManager().leavePlayer(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    @EventHandler
    public void handleInventory(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;

        ArenaController arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null) return;

        if (arena.getStatus() == ArenaController.ArenaStatus.STARTING ||
                (arena.getStatus() == ArenaController.ArenaStatus.PLAYING && arena.isInside(player.getLocation()))) {

            e.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory(InventoryCloseEvent.Reason.CANT_USE));
            plugin.getChatService().sendMessage(player, "YOU HAVE NO POWER HERE!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        ArenaController arena = plugin.getArenaManager().getArenaByPlayer(player);
        if (arena == null) return;

        if (arena.getStatus() == ArenaController.ArenaStatus.STARTING ||
                (arena.getStatus() == ArenaController.ArenaStatus.PLAYING && arena.isInside(player.getLocation()))) {

            e.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory(InventoryCloseEvent.Reason.CANT_USE));
            plugin.getChatService().sendMessage(player, "YOU HAVE NO POWER HERE!");
        }
    }

    public void startBlinkChecker() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                ArenaController arena = plugin.getArenaManager().getArenaByPlayer(player);
                if (arena == null || arena.getStatus() != ArenaController.ArenaStatus.PLAYING) continue;

                long lastSeen = lastPacketTime.getOrDefault(player.getUniqueId(), now);
                if (now - lastSeen > 3000) {
                    if (player.isFlying()) {
                        player.setVelocity(new org.bukkit.util.Vector(0, -1, 0));
                    }

                    if (now - lastSeen > 5000) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            arena.eliminatePlayer(player);
                            player.sendMessage("§cСоединение потеряно или абуз Blink!");
                        });
                    }
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onMovePacket(PlayerMoveEvent e) {
        lastPacketTime.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }



}

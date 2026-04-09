package com.astaro.mixGame.Events;

import com.astaro.mixGame.Game.ArenaController;
import com.astaro.mixGame.MixGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GameListener implements Listener {

    private final MixGame plugin;


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

        if(item == null || item.getType() == Material.AIR) return;

        if(item.isSimilar(plugin.getItemService().getLeaveItem())){
            e.setCancelled(true);
            plugin.getArenaManager().leavePlayer(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

}

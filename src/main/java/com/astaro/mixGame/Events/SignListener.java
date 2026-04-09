package com.astaro.mixGame.Events;

import com.astaro.mixGame.MixGame;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignListener implements Listener {
   private final MixGame plugin;

   public SignListener(MixGame plugin) {
      this.plugin = plugin;
   }

   @EventHandler(ignoreCancelled = true)
   public void onSignBreak(BlockBreakEvent event) {
      if (!(event.getBlock().getState() instanceof Sign)) return;

      String arenaName = plugin.getSignManager().getArenaBySign(event.getBlock().getLocation());
      if (arenaName == null) return;

      if (!event.getPlayer().hasPermission("mixgame.admin")) {
         event.setCancelled(true);
         plugin.getChatService().sendMessage(event.getPlayer(), "ErrorMessages.noPermission");
         return;
      }

      plugin.getSignManager().unregisterSign(event.getBlock().getLocation());
      plugin.getChatService().sendMessage(event.getPlayer(), "PlayerMessages.signRemoved", "%arena%", arenaName);
   }
}

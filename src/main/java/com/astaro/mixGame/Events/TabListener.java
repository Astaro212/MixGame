package com.astaro.mixGame.Events;

import com.astaro.mixGame.MixGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListener implements Listener {
   private final MixGame plugin;

   public TabListener(MixGame plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      if (plugin.getSettings().tabListEnabled()) {
         plugin.getChatService().updateTabList(event.getPlayer());
      }
   }
}

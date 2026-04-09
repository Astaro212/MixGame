package com.astaro.mixGame.Events;

import com.astaro.mixGame.MixGame;
import com.astaro.mixGame.data.SetupSession;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class SelectionListener implements Listener {
   private final MixGame plugin;
   private final NamespacedKey markerKey;

   public SelectionListener(MixGame plugin) {
      this.plugin = plugin;
      this.markerKey = new NamespacedKey(plugin, "marker");
   }

   @EventHandler
   public void onSelect(PlayerInteractEvent event) {
      ItemStack item = event.getItem();
      if (item == null || item.getType() != Material.SHEARS) return;

      if (!item.getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE)) return;

      event.setCancelled(true);

      SetupSession session = plugin.getSetupManager().getSession(event.getPlayer().getUniqueId());
      if (session == null) {
         plugin.getChatService().sendMessage(event.getPlayer(), "ErrorMessages.noActiveSession");
         return;
      }

      if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
         session.setLoc1(event.getClickedBlock().getLocation());
         plugin.getChatService().sendMessage(event.getPlayer(), "Setup.pos1Set");
      } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         session.setLoc2(event.getClickedBlock().getLocation());
         plugin.getChatService().sendMessage(event.getPlayer(), "Setup.pos2Set");
      }
   }
}

package com.astaro.mixGame.Signs;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class SignUtils {


   public static Block getTargetSign(Player player, int distance) {
      RayTraceResult result = player.rayTraceBlocks(distance);

      if (result == null || result.getHitBlock() == null) {
         return null;
      }

      Block block = result.getHitBlock();

      if (block.getState() instanceof Sign) {
         return block;
      }

      return null;
   }
}

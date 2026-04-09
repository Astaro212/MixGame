package com.astaro.mixGame.services;

import com.astaro.mixGame.MixGame;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemService {

    private final MixGame plugin;

    public ItemService(MixGame plugin) {
        this.plugin = plugin;
    }

    public ItemStack getLeaveItem(){
        ItemStack stack = new ItemStack(plugin.getSettings().leaveItem());
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text("Покинуть арену"));
        stack.setAmount(1);
        stack.setItemMeta(meta);
        return stack;
    }
}

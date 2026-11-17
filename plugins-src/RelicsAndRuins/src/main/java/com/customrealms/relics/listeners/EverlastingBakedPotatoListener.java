package com.customrealms.relics.listeners;

import com.customrealms.relics.items.CustomItemRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EverlastingBakedPotatoListener implements Listener {
    private final CustomItemRegistry registry;

    public EverlastingBakedPotatoListener(CustomItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!CustomItemRegistry.EVERLASTING_BAKED_POTATO_ID.equals(registry.getCustomId(event.getItem()))) return;

        Player player = event.getPlayer();
        boolean offHand = event.getHand() == EquipmentSlot.OFF_HAND;
        ItemStack potato = offHand
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
        if (!CustomItemRegistry.EVERLASTING_BAKED_POTATO_ID.equals(registry.getCustomId(potato))) return;

        event.setCancelled(true);
        int usesLeft = registry.getEverlastingBakedPotatoUses(potato);
        if (usesLeft <= 1) {
            if (offHand) {
                player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
        } else {
            registry.setEverlastingBakedPotatoUses(potato, usesLeft - 1);
        }

        int foodLevel = Math.min(20, player.getFoodLevel() + 5);
        player.setFoodLevel(foodLevel);
        player.setSaturation(Math.min(foodLevel, player.getSaturation() + 6.0f));
    }
}

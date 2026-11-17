package com.eagler.bounties;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BountyShopListener implements Listener {

    private final JavaPlugin plugin;
    private final EcoBridge ecoBridge;
    private final BountyManager bountyManager;
    private final BlackMarketGui blackMarketGui;

    public BountyShopListener(JavaPlugin plugin, EcoBridge ecoBridge, BountyManager bountyManager, BlackMarketGui blackMarketGui) {
        this.plugin = plugin;
        this.ecoBridge = ecoBridge;
        this.bountyManager = bountyManager;
        this.blackMarketGui = blackMarketGui;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bountyManager.getOrAssign(event.getPlayer());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            bountyManager.recordKill(killer, event.getEntity());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(BlackMarketGui.SHOP_TITLE)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int clickedSlot = event.getRawSlot();
        for (BlackMarketGui.ShopListing listing : blackMarketGui.getListings()) {
            if (listing.slot() != clickedSlot) continue;

            ItemStack purchased = blackMarketGui.buildRewardItem(listing);
            if (!ecoBridge.withdraw(player.getUniqueId(), listing.price())) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(ChatColor.RED + "Insufficient funds! You need $" + String.format("%.2f", listing.price()) + ".");
                return;
            }

            var leftovers = player.getInventory().addItem(purchased);
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.3f);
            player.sendMessage(ChatColor.GREEN + "Purchased " + ChatColor.YELLOW + listing.displayTitle()
                    + ChatColor.GREEN + " for $" + String.format("%.2f", listing.price()) + "!");

            blackMarketGui.openShop(player);
            return;
        }
    }
}

package com.eagler.bounties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class BlackMarketGui {

    public record ShopListing(
            int slot,
            String id,
            String category,
            double price,
            int amount,
            String displayTitle
    ) {}

    public static final String SHOP_TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Artisan Black Market";

    private final EcoBridge ecoBridge;
    private final List<ShopListing> listings = List.of(
            new ShopListing(10, "bandage_wrap", "GEAR", 65.0, 3, "3x Field Medic Bandage"),
            new ShopListing(11, "vitality_elixir", "GEAR", 280.0, 1, "Elixir of Immortality"),
            new ShopListing(12, "crimson_heart_fruit", "GEAR", 750.0, 1, "Crimson Heart Crystal (+1 Max Heart)"),
            new ShopListing(14, "telekinesis", "BOOK", 320.0, 1, "Telekinesis I Book"),
            new ShopListing(15, "auto_smelt", "BOOK", 360.0, 1, "Auto-Smelt I Book"),
            new ShopListing(16, "grapple", "BOOK", 400.0, 1, "Grapple I Book"),
            new ShopListing(20, "ignis_crystal", "BOSS_MAT", 450.0, 1, "Summoning Crystal: Ignis"),
            new ShopListing(21, "boreas_crystal", "BOSS_MAT", 450.0, 1, "Summoning Crystal: Boreas"),
            new ShopListing(23, "malakor_crystal", "BOSS_MAT", 500.0, 1, "Summoning Crystal: Malakor"),
            new ShopListing(24, "goliath_crystal", "BOSS_MAT", 550.0, 1, "Summoning Crystal: Goliath")
    );

    public BlackMarketGui(EcoBridge ecoBridge) {
        this.ecoBridge = ecoBridge;
    }

    public List<ShopListing> getListings() {
        return listings;
    }

    public ItemStack buildRewardItem(ShopListing listing) {
        return switch (listing.category()) {
            case "GEAR" -> ecoBridge.createCustomGear(listing.id(), listing.amount());
            case "BOOK" -> ecoBridge.createCustomBook(listing.id(), 1);
            case "BOSS_MAT" -> ecoBridge.createBossMaterial(listing.id(), listing.amount());
            default -> new ItemStack(Material.PAPER);
        };
    }

    public void openShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 36, SHOP_TITLE);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gMeta = glass.getItemMeta();
        if (gMeta != null) {
            gMeta.setDisplayName(" ");
            glass.setItemMeta(gMeta);
        }
        for (int i = 0; i < 36; i++) gui.setItem(i, glass);

        for (ShopListing listing : listings) {
            ItemStack icon = buildRewardItem(listing);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() && meta.getLore() != null
                        ? new ArrayList<>(meta.getLore())
                        : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.GREEN + "$" + String.format("%.2f", listing.price()));
                lore.add(ChatColor.YELLOW + "Click to purchase!");
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            gui.setItem(listing.slot(), icon);
        }

        ItemStack balIcon = new ItemStack(Material.EMERALD);
        ItemMeta bMeta = balIcon.getItemMeta();
        if (bMeta != null) {
            double bal = ecoBridge.getBalance(player.getUniqueId());
            bMeta.setDisplayName(ChatColor.GREEN + "Your Balance: $" + String.format("%.2f", bal));
            bMeta.setLore(List.of(
                    ChatColor.GRAY + "Earn money via /bounty hunts",
                    ChatColor.GRAY + "or by selling items on /market!"
            ));
            balIcon.setItemMeta(bMeta);
        }
        gui.setItem(31, balIcon);

        player.openInventory(gui);
    }
}

package com.eagler.guide;

import com.eagler.guide.GuideEntry.Category;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuideGuiManager implements Listener {

    private final GuideKnowledgeBase knowledgeBase;
    public static final String MAIN_TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
            + "Server Encyclopedia (/guide)";
    public static final String CATEGORY_PREFIX = ChatColor.DARK_BLUE + "Guide: ";

    public GuideGuiManager(GuideKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, MAIN_TITLE);
        ItemStack pane = makeItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, pane);
        }

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        Category[] categories = Category.values();
        for (int index = 0; index < categories.length && index < slots.length; index++) {
            Category category = categories[index];
            int count = knowledgeBase.getByCategory(category).size();
            inventory.setItem(slots[index], makeItem(category.getIcon(),
                    ChatColor.GOLD + "" + ChatColor.BOLD + category.getDisplayName(),
                    List.of(
                            ChatColor.GRAY + "Topics inside: " + ChatColor.AQUA + count,
                            ChatColor.YELLOW + "Click to view details!"
                    )));
        }

        inventory.setItem(22, makeItem(Material.BOOK,
                ChatColor.GREEN + "Tip: Ask Any Question in Chat!",
                List.of(
                        ChatColor.WHITE + "Type " + ChatColor.AQUA + "/ai <question>" + ChatColor.WHITE
                                + " or " + ChatColor.AQUA + "/guide <topic>",
                        ChatColor.GRAY + "Examples:",
                        ChatColor.GRAY + "• /ai how to summon ignis",
                        ChatColor.GRAY + "• /ai titan siege helm",
                        ChatColor.GRAY + "• /ai grapple enchant"
                )));

        player.openInventory(inventory);
    }

    public void openCategoryMenu(Player player, Category category) {
        Inventory inventory = Bukkit.createInventory(null, 36, CATEGORY_PREFIX + category.getDisplayName());
        ItemStack pane = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, pane);
        }

        List<GuideEntry> entries = knowledgeBase.getByCategory(category);
        int slot = 10;
        for (GuideEntry entry : entries) {
            if (slot == 17) {
                slot = 19;
            }
            if (slot >= 26) {
                break;
            }
            List<String> lore = new ArrayList<>(entry.details());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to print this guide to chat!");
            inventory.setItem(slot++, makeItem(entry.icon(), entry.title(), lore));
        }

        inventory.setItem(31, makeItem(Material.ARROW, ChatColor.RED + "← Back to Main Guide Menu", List.of()));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(MAIN_TITLE) && !title.startsWith(CATEGORY_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) {
            return;
        }

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (title.equals(MAIN_TITLE)) {
            for (Category category : Category.values()) {
                if (category.getDisplayName().equals(name)) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
                    openCategoryMenu(player, category);
                    return;
                }
            }
        } else if (clicked.getType() == Material.ARROW) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
            openMainMenu(player);
        } else {
            for (GuideEntry entry : knowledgeBase.getByCategory(categoryFromTitle(title))) {
                if (ChatColor.stripColor(entry.title()).equals(name)) {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
                    player.sendMessage(ChatColor.GOLD + "=== " + entry.title() + ChatColor.GOLD + " ===");
                    for (String line : entry.details()) {
                        player.sendMessage(line);
                    }
                    return;
                }
            }
        }
    }

    private Category categoryFromTitle(String title) {
        String categoryName = title.substring(CATEGORY_PREFIX.length());
        for (Category category : Category.values()) {
            if (category.getDisplayName().equals(categoryName)) {
                return category;
            }
        }
        return null;
    }

    private ItemStack makeItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}

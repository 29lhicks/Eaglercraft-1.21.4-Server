package com.eagler.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeGui {

    public static final String GUI_TITLE = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "RPG Skill Tree";

    private final SkillManager skillManager;

    public SkillTreeGui(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    public void openSkillTree(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, GUI_TITLE);

        ItemStack bg = filler(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 45; i++) gui.setItem(i, bg);

        // Branch Headers (Row 1)
        gui.setItem(1, header(Material.NETHERITE_SWORD, ChatColor.RED + "" + ChatColor.BOLD + "WARLORD BRANCH", "Melee Damage, Crits & Lifesteal"));
        gui.setItem(4, header(Material.TOTEM_OF_UNDYING, ChatColor.GREEN + "" + ChatColor.BOLD + "GUARDIAN BRANCH", "Max Hearts, Armor & Death Save"));
        gui.setItem(7, header(Material.FEATHER, ChatColor.AQUA + "" + ChatColor.BOLD + "PATHFINDER BRANCH", "Speed, Bonus EXP & Evasion"));

        // Connector Glass Panes between Tiers
        gui.setItem(19 - 9, filler(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "↓ Tier 1 → Tier 2"));
        gui.setItem(22 - 9, filler(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "↓ Tier 1 → Tier 2"));
        gui.setItem(25 - 9, filler(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ChatColor.AQUA + "↓ Tier 1 → Tier 2"));

        // Render All 9 Skill Nodes
        int availablePoints = skillManager.getSkillPoints(player);
        for (SkillNode node : SkillNode.values()) {
            int rank = skillManager.getRank(player, node);
            boolean maxed = rank >= node.getMaxRank();
            boolean unlockedPrereq = skillManager.hasUnlockedPrerequisite(player, node);

            Material displayMat = !unlockedPrereq ? Material.GRAY_DYE : node.getIcon();
            ItemStack item = new ItemStack(displayMat);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String statusTag = maxed
                        ? ChatColor.GOLD + " [MAXED]"
                        : (!unlockedPrereq ? ChatColor.DARK_GRAY + " [LOCKED]" : ChatColor.GREEN + " [" + rank + "/" + node.getMaxRank() + "]");

                meta.setDisplayName(node.getDisplayName() + statusTag);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

                List<String> lore = new ArrayList<>(node.getDescription());
                lore.add("");
                lore.add(ChatColor.GRAY + "Current Rank: " + ChatColor.WHITE + rank + " / " + node.getMaxRank());

                if (!unlockedPrereq) {
                    SkillNode req = SkillNode.fromId(node.getPrerequisiteId());
                    lore.add(ChatColor.RED + "Requires Rank 1 in: " + (req != null ? ChatColor.stripColor(req.getDisplayName()) : "Previous Tier"));
                } else if (maxed) {
                    lore.add(ChatColor.GOLD + "Maximum rank mastered!");
                } else {
                    lore.add(ChatColor.GOLD + "Upgrade Cost: " + ChatColor.YELLOW + node.getCostPerRank() + " Skill Point(s)");
                    if (availablePoints >= node.getCostPerRank()) {
                        lore.add(ChatColor.GREEN + "▶ Click to upgrade!");
                    } else {
                        lore.add(ChatColor.RED + "Not enough Skill Points!");
                    }
                }

                if (rank > 0) {
                    meta.setEnchantmentGlintOverride(true);
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(node.getGuiSlot(), item);
        }

        // Bottom Bar: Skill Points Counter (Slot 39) & Reset Button (Slot 41)
        ItemStack pointsIcon = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta pMeta = pointsIcon.getItemMeta();
        if (pMeta != null) {
            pMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Available Skill Points: " + ChatColor.AQUA + availablePoints);
            pMeta.setLore(List.of(
                    ChatColor.GRAY + "How to earn Skill Points:",
                    ChatColor.YELLOW + "• Reach Level 40 EXP (+1 Point & resets to Lvl 0)",
                    ChatColor.YELLOW + "• Defeat any World Boss (+1 Point)",
                    ChatColor.DARK_GRAY + "Total Spent: " + skillManager.getTotalSpentPoints(player) + " Points"
            ));
            pointsIcon.setItemMeta(pMeta);
        }
        gui.setItem(39, pointsIcon);

        ItemStack resetIcon = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = resetIcon.getItemMeta();
        if (rMeta != null) {
            rMeta.setDisplayName(ChatColor.RED + "Reset & Refund Skill Points");
            rMeta.setLore(List.of(
                    ChatColor.GRAY + "Refunds all " + skillManager.getTotalSpentPoints(player) + " spent points.",
                    ChatColor.YELLOW + "Cost: 10 EXP Levels",
                    ChatColor.RED + "Click to reset your build!"
            ));
            resetIcon.setItemMeta(rMeta);
        }
        gui.setItem(41, resetIcon);

        player.openInventory(gui);
    }

    private ItemStack filler(Material mat, String name) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack header(Material mat, String title, String subtitle) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(List.of(ChatColor.GRAY + subtitle));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}

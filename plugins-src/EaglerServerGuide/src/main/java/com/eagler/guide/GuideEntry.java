package com.eagler.guide;

import org.bukkit.Material;

import java.util.List;

public record GuideEntry(
        String id,
        Category category,
        String title,
        Material icon,
        List<String> keywords,
        List<String> details
) {
    public enum Category {
        GETTING_STARTED("Getting Started & Commands", Material.NETHER_STAR),
        RELICS("Relics & Ruins (24 Items)", Material.ECHO_SHARD),
        CUSTOM_GEAR("Custom Armor Sets & Gear", Material.NETHERITE_CHESTPLATE),
        BOSSES("World Bosses & Elites", Material.WITHER_SKELETON_SKULL),
        ENCHANTS("Custom Enchantments (20)", Material.ENCHANTED_BOOK),
        STRUCTURES("Worldgen Structures (9)", Material.MOSSY_STONE_BRICKS),
        ECONOMY_SKILLS("Economy, Skills & Bounties", Material.EMERALD);

        private final String displayName;
        private final Material icon;

        Category(String displayName, Material icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Material getIcon() {
            return icon;
        }
    }
}

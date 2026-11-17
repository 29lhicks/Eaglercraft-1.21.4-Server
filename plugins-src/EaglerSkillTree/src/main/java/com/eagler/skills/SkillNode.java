package com.eagler.skills;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

public enum SkillNode {

    // ==================== BRANCH 1: COMBAT (WARLORD) ====================
    COMBAT_MIGHT(
            "combat_might", Branch.COMBAT, 1, 10,
            ChatColor.RED + "Brutal Strength", Material.IRON_SWORD, 5, 1, null,
            List.of(ChatColor.GRAY + "Increases melee weapon damage.", ChatColor.YELLOW + "+1.0 Attack Damage per rank (Max +5.0)")
    ),
    COMBAT_CRIT(
            "combat_crit", Branch.COMBAT, 2, 19,
            ChatColor.RED + "Executioner's Edge", Material.NETHERITE_AXE, 3, 2, "combat_might",
            List.of(ChatColor.GRAY + "Deal bonus damage to wounded targets.", ChatColor.YELLOW + "+8% Crit/Low-HP Damage per rank")
    ),
    COMBAT_LIFESTEAL(
            "combat_lifesteal", Branch.COMBAT, 3, 28,
            ChatColor.DARK_RED + "Sanguine Thirst", Material.REDSTONE, 3, 2, "combat_crit",
            List.of(ChatColor.GRAY + "Stacks with Soulreaver & Lifesteal.", ChatColor.YELLOW + "Heals +0.5 Hearts on melee hit per rank")
    ),

    // ==================== BRANCH 2: VITALITY (GUARDIAN) ====================
    VIT_HEALTH(
            "vit_health", Branch.VITALITY, 1, 13,
            ChatColor.GREEN + "Titan's Constitution", Material.GOLDEN_APPLE, 5, 1, null,
            List.of(ChatColor.GRAY + "Stacks cleanly with Armor Sets & Heart Crystals.", ChatColor.YELLOW + "+1 Max Heart (+2 HP) per rank (Max +5 Hearts)")
    ),
    VIT_ARMOR(
            "vit_armor", Branch.VITALITY, 2, 22,
            ChatColor.GREEN + "Obsidian Skin", Material.NETHERITE_CHESTPLATE, 4, 2, "vit_health",
            List.of(ChatColor.GRAY + "Reduces all incoming combat damage.", ChatColor.YELLOW + "+4% Damage Reduction per rank (Max 16%)")
    ),
    VIT_SECOND_WIND(
            "vit_second_wind", Branch.VITALITY, 3, 31,
            ChatColor.LIGHT_PURPLE + "Phoenix Rebirth", Material.TOTEM_OF_UNDYING, 1, 3, "vit_armor",
            List.of(ChatColor.GRAY + "Survive fatal blows without a Totem.", ChatColor.YELLOW + "Saves you from death at 4 Hearts (3m Cooldown)")
    ),

    // ==================== BRANCH 3: AGILITY & FORTUNE (PATHFINDER) ====================
    AGI_SPEED(
            "agi_speed", Branch.AGILITY, 1, 16,
            ChatColor.AQUA + "Windrunner Stride", Material.FEATHER, 4, 1, null,
            List.of(ChatColor.GRAY + "Stacks on top of Zephyr & Juggernaut armor.", ChatColor.YELLOW + "+6% Movement Speed per rank (Max +24%)")
    ),
    AGI_FORTUNE(
            "agi_fortune", Branch.AGILITY, 2, 25,
            ChatColor.GOLD + "Treasure Hunter", Material.EMERALD, 3, 2, "agi_speed",
            List.of(ChatColor.GRAY + "Boosts EXP and rare drops from mobs.", ChatColor.YELLOW + "+25% Bonus EXP & Double Drop chance per rank")
    ),
    AGI_EVASION(
            "agi_evasion", Branch.AGILITY, 3, 34,
            ChatColor.AQUA + "Phantom Reflexes", Material.ENDER_PEARL, 3, 2, "agi_fortune",
            List.of(ChatColor.GRAY + "Chance to completely dodge incoming attacks.", ChatColor.YELLOW + "+6% Dodge Chance per rank (Max 18%)")
    );

    public enum Branch {
        COMBAT("Warlord Branch", ChatColor.RED),
        VITALITY("Guardian Branch", ChatColor.GREEN),
        AGILITY("Pathfinder Branch", ChatColor.AQUA);

        private final String title;
        private final ChatColor color;

        Branch(String title, ChatColor color) {
            this.title = title;
            this.color = color;
        }

        public String getTitle() { return title; }
        public ChatColor getColor() { return color; }
    }

    private final String id;
    private final Branch branch;
    private final int tier;
    private final int guiSlot;
    private final String displayName;
    private final Material icon;
    private final int maxRank;
    private final int costPerRank;
    private final String prerequisiteId;
    private final List<String> description;

    SkillNode(
            String id, Branch branch, int tier, int guiSlot,
            String displayName, Material icon, int maxRank, int costPerRank,
            String prerequisiteId, List<String> description
    ) {
        this.id = id;
        this.branch = branch;
        this.tier = tier;
        this.guiSlot = guiSlot;
        this.displayName = displayName;
        this.icon = icon;
        this.maxRank = maxRank;
        this.costPerRank = costPerRank;
        this.prerequisiteId = prerequisiteId;
        this.description = description;
    }

    public String getId() { return id; }
    public Branch getBranch() { return branch; }
    public int getTier() { return tier; }
    public int getGuiSlot() { return guiSlot; }
    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
    public int getMaxRank() { return maxRank; }
    public int getCostPerRank() { return costPerRank; }
    public String getPrerequisiteId() { return prerequisiteId; }
    public List<String> getDescription() { return description; }

    public static SkillNode fromId(String id) {
        if (id == null) return null;
        for (SkillNode node : values()) {
            if (node.id.equalsIgnoreCase(id)) return node;
        }
        return null;
    }

    public static SkillNode fromSlot(int slot) {
        for (SkillNode node : values()) {
            if (node.guiSlot == slot) return node;
        }
        return null;
    }
}

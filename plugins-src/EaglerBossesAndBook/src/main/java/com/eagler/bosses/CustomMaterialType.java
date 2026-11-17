package com.eagler.bosses;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

public enum CustomMaterialType {
    // === 4 BOSS / ELITE CRAFTING MATERIALS ===
    INFERNAL_CORE(
            "infernal_core",
            ChatColor.RED + "Infernal Core",
            Material.MAGMA_CREAM,
            false,
            null,
            List.of(
                    ChatColor.GRAY + "A blazing heart torn from Ignis or Cinder Knights.",
                    ChatColor.YELLOW + "Used to craft Infernal Summoning Crystals & high-tier gear."
            )
    ),
    TEMPEST_ESSENCE(
            "tempest_essence",
            ChatColor.AQUA + "Tempest Essence",
            Material.WIND_CHARGE,
            false,
            null,
            List.of(
                    ChatColor.GRAY + "Condensed gale energy dropped by Boreas & Gale Stalkers.",
                    ChatColor.YELLOW + "Used to craft Stormwing Summoning Crystals."
            )
    ),
    ABYSSAL_SOUL_SHARD(
            "abyssal_soul_shard",
            ChatColor.DARK_PURPLE + "Abyssal Soul Shard",
            Material.ECHO_SHARD,
            false,
            null,
            List.of(
                    ChatColor.GRAY + "A dark fragment harvested from Malakor & Void Acolytes.",
                    ChatColor.YELLOW + "Used to craft Void Reaper Summoning Crystals."
            )
    ),
    TITAN_HEARTSTONE(
            "titan_heartstone",
            ChatColor.DARK_GRAY + "Titan Heartstone",
            Material.HEAVY_CORE,
            false,
            null,
            List.of(
                    ChatColor.GRAY + "An indestructible core dropped by Goliath & Obsidian Sentinels.",
                    ChatColor.YELLOW + "Used to craft Colossus Summoning Crystals."
            )
    ),

    // === 4 CRAFTABLE BOSS SUMMONING CRYSTALS ===
    IGNIS_CRYSTAL(
            "ignis_crystal",
            ChatColor.GOLD + "Summoning Crystal: Ignis",
            Material.FIRE_CHARGE,
            true,
            BossType.IGNIS,
            List.of(
                    ChatColor.RED + "Right-click the ground to summon Ignis, The Magma Sovereign!",
                    ChatColor.GRAY + "Recipe: 4 Blaze Powder + 4 Magma Cream + 1 Eye of Ender"
            )
    ),
    BOREAS_CRYSTAL(
            "boreas_crystal",
            ChatColor.AQUA + "Summoning Crystal: Boreas",
            Material.HEART_OF_THE_SEA,
            true,
            BossType.BOREAS,
            List.of(
                    ChatColor.AQUA + "Right-click the ground to summon Boreas, Stormwing Warden!",
                    ChatColor.GRAY + "Recipe: 4 Breeze Rods + 4 Phantom Membranes + 1 Eye of Ender"
            )
    ),
    MALAKOR_CRYSTAL(
            "malakor_crystal",
            ChatColor.DARK_PURPLE + "Summoning Crystal: Malakor",
            Material.ENDER_EYE,
            true,
            BossType.MALAKOR,
            List.of(
                    ChatColor.DARK_PURPLE + "Right-click the ground to summon Malakor, Void Reaper!",
                    ChatColor.GRAY + "Recipe: 4 Ender Pearls + 4 Amethyst Shards + 1 Eye of Ender"
            )
    ),
    GOLIATH_CRYSTAL(
            "goliath_crystal",
            ChatColor.GRAY + "Summoning Crystal: Goliath",
            Material.NETHER_STAR,
            true,
            BossType.GOLIATH,
            List.of(
                    ChatColor.DARK_GRAY + "Right-click the ground to summon Goliath, Obsidian Colossus!",
                    ChatColor.GRAY + "Recipe: 4 Obsidian + 4 Iron Blocks + 1 Eye of Ender"
            )
    );

    private final String id;
    private final String displayName;
    private final Material material;
    private final boolean crystal;
    private final BossType summonedBoss;
    private final List<String> lore;

    CustomMaterialType(String id, String displayName, Material material, boolean crystal, BossType summonedBoss, List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.crystal = crystal;
        this.summonedBoss = summonedBoss;
        this.lore = lore;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public boolean isCrystal() { return crystal; }
    public BossType getSummonedBoss() { return summonedBoss; }
    public List<String> getLore() { return lore; }

    public static CustomMaterialType fromId(String input) {
        for (CustomMaterialType type : values()) {
            if (type.id.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}

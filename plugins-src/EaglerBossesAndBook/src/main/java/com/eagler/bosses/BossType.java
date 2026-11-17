package com.eagler.bosses;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.EntityType;

import java.util.List;

public enum BossType {
    IGNIS(
            "ignis",
            ChatColor.RED + "" + ChatColor.BOLD + "Ignis, The Magma Sovereign",
            EntityType.WITHER_SKELETON,
            320.0,
            13.0,
            0.30,
            BarColor.RED,
            CustomMaterialType.INFERNAL_CORE,
            List.of("pyro_crown", "pyro_chestplate", "pyro_leggings", "pyro_boots", "executioner_greataxe"),
            List.of("molten", "auto_smelt", "lava_walker")
    ),
    BOREAS(
            "boreas",
            ChatColor.AQUA + "" + ChatColor.BOLD + "Boreas, Stormwing Warden",
            EntityType.STRAY,
            280.0,
            11.0,
            0.36,
            BarColor.BLUE,
            CustomMaterialType.TEMPEST_ESSENCE,
            List.of("zephyr_hood", "zephyr_tunic", "zephyr_leggings", "zephyr_boots", "valkyrie_spear", "skyhook_rod"),
            List.of("grapple", "thunderstrike", "gears", "springs")
    ),
    MALAKOR(
            "malakor",
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Malakor, Void Reaper",
            EntityType.ENDERMAN,
            300.0,
            14.0,
            0.34,
            BarColor.PURPLE,
            CustomMaterialType.ABYSSAL_SOUL_SHARD,
            List.of("shadow_mask", "shadow_cloak", "shadow_leggings", "shadow_boots", "soulreaver_scythe", "phantom_rapier"),
            List.of("lifesteal", "frostbite", "poison_aspect", "telekinesis")
    ),
    GOLIATH(
            "goliath",
            ChatColor.GOLD + "" + ChatColor.BOLD + "Goliath, Obsidian Colossus",
            EntityType.IRON_GOLEM,
            420.0,
            16.0,
            0.24,
            BarColor.YELLOW,
            CustomMaterialType.TITAN_HEARTSTONE,
            List.of("juggernaut_helmet", "juggernaut_chestplate", "juggernaut_leggings", "juggernaut_boots", "earthshaker_pickaxe"),
            List.of("hardened", "veinminer", "saturation", "experience")
    );

    private final String id;
    private final String displayName;
    private final EntityType entityType;
    private final double maxHealth;
    private final double attackDamage;
    private final double moveSpeed;
    private final BarColor barColor;
    private final CustomMaterialType guaranteedMaterial;
    private final List<String> customGearRewards;
    private final List<String> customEnchantRewards;

    BossType(
            String id, String displayName, EntityType entityType,
            double maxHealth, double attackDamage, double moveSpeed,
            BarColor barColor, CustomMaterialType guaranteedMaterial,
            List<String> customGearRewards, List<String> customEnchantRewards
    ) {
        this.id = id;
        this.displayName = displayName;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.moveSpeed = moveSpeed;
        this.barColor = barColor;
        this.guaranteedMaterial = guaranteedMaterial;
        this.customGearRewards = customGearRewards;
        this.customEnchantRewards = customEnchantRewards;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public EntityType getEntityType() { return entityType; }
    public double getMaxHealth() { return maxHealth; }
    public double getAttackDamage() { return attackDamage; }
    public double getMoveSpeed() { return moveSpeed; }
    public BarColor getBarColor() { return barColor; }
    public CustomMaterialType getGuaranteedMaterial() { return guaranteedMaterial; }
    public List<String> getCustomGearRewards() { return customGearRewards; }
    public List<String> getCustomEnchantRewards() { return customEnchantRewards; }

    public static BossType fromId(String input) {
        for (BossType type : values()) {
            if (type.id.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}

package com.eagler.bosses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;

public enum EliteMobType {
    CINDER_KNIGHT(
            "cinder_knight",
            ChatColor.RED + "✦ Cinder Knight",
            EntityType.ZOMBIE,
            65.0,
            8.0,
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_SWORD,
            CustomMaterialType.INFERNAL_CORE,
            List.of("pyro_boots", "pyro_crown"),
            List.of("molten", "auto_smelt")
    ),
    GALE_STALKER(
            "gale_stalker",
            ChatColor.AQUA + "✦ Gale Stalker",
            EntityType.SKELETON,
            55.0,
            7.0,
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.BOW,
            CustomMaterialType.TEMPEST_ESSENCE,
            List.of("zephyr_boots", "skyhook_rod"),
            List.of("grapple", "gears", "springs")
    ),
    VOID_ACOLYTE(
            "void_acolyte",
            ChatColor.DARK_PURPLE + "✦ Void Acolyte",
            EntityType.HUSK,
            60.0,
            8.5,
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.IRON_SWORD,
            CustomMaterialType.ABYSSAL_SOUL_SHARD,
            List.of("shadow_boots", "phantom_rapier"),
            List.of("lifesteal", "poison_aspect")
    ),
    OBSIDIAN_SENTINEL(
            "obsidian_sentinel",
            ChatColor.DARK_GRAY + "✦ Obsidian Sentinel",
            EntityType.ZOMBIE,
            85.0,
            9.5,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_AXE,
            CustomMaterialType.TITAN_HEARTSTONE,
            List.of("juggernaut_boots", "earthshaker_pickaxe"),
            List.of("hardened", "veinminer")
    );

    private final String id;
    private final String displayName;
    private final EntityType entityType;
    private final double maxHealth;
    private final double attackDamage;
    private final Material helmet;
    private final Material chestplate;
    private final Material weapon;
    private final CustomMaterialType dropMaterial;
    private final List<String> rareGearDrops;
    private final List<String> rareEnchantDrops;

    EliteMobType(
            String id, String displayName, EntityType entityType,
            double maxHealth, double attackDamage,
            Material helmet, Material chestplate, Material weapon,
            CustomMaterialType dropMaterial,
            List<String> rareGearDrops, List<String> rareEnchantDrops
    ) {
        this.id = id;
        this.displayName = displayName;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.weapon = weapon;
        this.dropMaterial = dropMaterial;
        this.rareGearDrops = rareGearDrops;
        this.rareEnchantDrops = rareEnchantDrops;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public EntityType getEntityType() { return entityType; }
    public double getMaxHealth() { return maxHealth; }
    public double getAttackDamage() { return attackDamage; }
    public Material getHelmet() { return helmet; }
    public Material getChestplate() { return chestplate; }
    public Material getWeapon() { return weapon; }
    public CustomMaterialType getDropMaterial() { return dropMaterial; }
    public List<String> getRareGearDrops() { return rareGearDrops; }
    public List<String> getRareEnchantDrops() { return rareEnchantDrops; }

    public static EliteMobType fromId(String input) {
        for (EliteMobType type : values()) {
            if (type.id.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}

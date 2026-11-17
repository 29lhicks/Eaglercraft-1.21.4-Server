package com.eagler.enchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public enum EnchantmentType {
    // Fishing Rod
    GRAPPLE("grapple", "Grapple", 2, TargetGroup.FISHING_ROD, ChatColor.AQUA, "Launches you toward your hooked bobber"),

    // Tools
    VEINMINER("veinminer", "Veinminer", 3, TargetGroup.PICKAXE, ChatColor.GOLD, "Mines connected veins of matching ores"),
    AUTO_SMELT("auto_smelt", "Auto-Smelt", 1, TargetGroup.MINING_TOOL, ChatColor.RED, "Automatically smelts mined ores and blocks"),
    TELEKINESIS("telekinesis", "Telekinesis", 1, TargetGroup.TOOL_OR_WEAPON, ChatColor.LIGHT_PURPLE, "Sends block and mob drops directly to your inventory"),
    TIMBER("timber", "Timber", 2, TargetGroup.AXE, ChatColor.DARK_GREEN, "Fells entire trees from the bottom log"),
    EXCAVATE("excavate", "Excavate", 2, TargetGroup.SHOVEL, ChatColor.YELLOW, "Digs a 3x3 area of soft blocks"),
    REPLENISH("replenish", "Replenish", 1, TargetGroup.HOE, ChatColor.GREEN, "Automatically replants harvested crops"),
    EXPERIENCE("experience", "Experience", 3, TargetGroup.TOOL_OR_WEAPON, ChatColor.GREEN, "Increases EXP dropped from blocks and mobs"),

    // Weapons
    LIFESTEAL("lifesteal", "Lifesteal", 3, TargetGroup.MELEE_WEAPON, ChatColor.DARK_RED, "Heals a portion of melee damage dealt"),
    THUNDERSTRIKE("thunderstrike", "Thunderstrike", 3, TargetGroup.MELEE_WEAPON, ChatColor.YELLOW, "Chance to strike targets with lightning"),
    FROSTBITE("frostbite", "Frostbite", 2, TargetGroup.MELEE_WEAPON, ChatColor.AQUA, "Slows and freezes enemies on hit"),
    POISON_ASPECT("poison_aspect", "Poison Aspect", 2, TargetGroup.MELEE_WEAPON, ChatColor.DARK_GREEN, "Inflicts poison on attacked targets"),
    EXPLOSIVE_ARROWS("explosive_arrows", "Explosive Arrows", 2, TargetGroup.BOW_OR_CROSSBOW, ChatColor.RED, "Arrows detonate with a safe terrain-friendly blast"),

    // Armor
    GLOWING("glowing", "Glowing", 1, TargetGroup.HELMET, ChatColor.YELLOW, "Grants permanent Night Vision while worn"),
    SATURATION("saturation", "Saturation", 3, TargetGroup.HELMET, ChatColor.GOLD, "Slowly restores hunger over time"),
    MOLTEN("molten", "Molten", 3, TargetGroup.CHESTPLATE, ChatColor.RED, "Ignites attackers who strike you"),
    HARDENED("hardened", "Hardened", 3, TargetGroup.CHESTPLATE, ChatColor.GRAY, "Chance to negate incoming armor durability loss"),
    GEARS("gears", "Gears", 2, TargetGroup.BOOTS, ChatColor.AQUA, "Grants permanent Speed while worn"),
    SPRINGS("springs", "Springs", 2, TargetGroup.BOOTS, ChatColor.GREEN, "Grants permanent Jump Boost while worn"),
    LAVA_WALKER("lava_walker", "Lava Walker", 2, TargetGroup.BOOTS, ChatColor.DARK_RED, "Solidifies nearby lava into temporary magma blocks"),

    // Additional balanced enchantments
    SOULBOUND("soulbound", "Soulbound", 1, TargetGroup.ANY_EQUIPMENT, ChatColor.DARK_PURPLE,
            "Keeps one enchanted item on death; Curse of Vanishing still applies"),
    HUNTER("hunter", "Hunter", 2, TargetGroup.MELEE_WEAPON, ChatColor.DARK_GREEN,
            "Deals 3% more damage to hostile mobs per level"),
    DUELIST("duelist", "Duelist", 2, TargetGroup.MELEE_WEAPON, ChatColor.BLUE,
            "Deals 2% more melee damage to players per level"),
    FINISHER("finisher", "Finisher", 2, TargetGroup.MELEE_WEAPON, ChatColor.RED,
            "Deals 0.5 bonus damage per level to targets below 25% health"),
    BACKSTAB("backstab", "Backstab", 2, TargetGroup.MELEE_WEAPON, ChatColor.DARK_RED,
            "Deals 5% more damage per level when striking from behind"),
    CRIPPLING("crippling", "Crippling", 2, TargetGroup.MELEE_WEAPON, ChatColor.GRAY,
            "Has a 5% chance per level to briefly slow a target"),
    STAGGER("stagger", "Stagger", 2, TargetGroup.MELEE_WEAPON, ChatColor.YELLOW,
            "Has a 5% chance per level to briefly knock a target back"),
    LONGSHOT("longshot", "Longshot", 2, TargetGroup.BOW_OR_CROSSBOW, ChatColor.AQUA,
            "Adds up to 5% ranged damage per level at long distance"),
    PINNING("pinning", "Pinning", 2, TargetGroup.BOW_OR_CROSSBOW, ChatColor.GRAY,
            "Has a 5% chance per level to briefly slow arrow targets"),
    RETRIEVAL("retrieval", "Retrieval", 2, TargetGroup.BOW_OR_CROSSBOW, ChatColor.GREEN,
            "Has a 10% chance per level to recover a fired arrow"),
    FIREGUARD("fireguard", "Fireguard", 2, TargetGroup.ARMOR, ChatColor.RED,
            "Reduces fire damage by 8% per level; strongest worn piece counts"),
    BLASTGUARD("blastguard", "Blastguard", 2, TargetGroup.ARMOR, ChatColor.GOLD,
            "Reduces explosion damage by 8% per level; strongest worn piece counts"),
    PROJECTILE_WARD("projectile_ward", "Projectile Ward", 2, TargetGroup.ARMOR, ChatColor.AQUA,
            "Reduces projectile damage by 8% per level; strongest worn piece counts"),
    FEATHERFALL("featherfall", "Featherfall", 3, TargetGroup.BOOTS, ChatColor.WHITE,
            "Reduces fall damage by 8% per level"),
    FROSTGUARD("frostguard", "Frostguard", 2, TargetGroup.ARMOR, ChatColor.BLUE,
            "Reduces freezing damage by 10% per level; strongest worn piece counts"),
    BULWARK("bulwark", "Bulwark", 2, TargetGroup.CHESTPLATE, ChatColor.GRAY,
            "Reduces incoming damage by 2% per level"),
    SECOND_WIND("second_wind", "Second Wind", 1, TargetGroup.CHESTPLATE, ChatColor.GREEN,
            "Survives one otherwise-fatal hit every two minutes, leaving you at 1 health"),
    SWIFT_SWIM("swift_swim", "Swift Swim", 1, TargetGroup.BOOTS, ChatColor.AQUA,
            "Grants Dolphin's Grace while swimming"),
    GILLS("gills", "Gills", 1, TargetGroup.HELMET, ChatColor.BLUE,
            "Grants Water Breathing while submerged"),
    RECYCLER("recycler", "Recycler", 2, TargetGroup.TOOL, ChatColor.GREEN,
            "Has a 5% chance per level to prevent tool durability loss"),
    PROSPECTOR("prospector", "Prospector", 2, TargetGroup.PICKAXE, ChatColor.GOLD,
            "Has a 5% chance per level to yield one extra ore drop"),
    ARCHAEOLOGIST("archaeologist", "Archaeologist", 2, TargetGroup.SHOVEL, ChatColor.YELLOW,
            "Has a 5% chance per level to yield one extra sand, gravel, or clay drop"),
    HORTICULTURIST("horticulturist", "Horticulturist", 2, TargetGroup.HOE, ChatColor.GREEN,
            "Has a 5% chance per level to harvest one extra crop from mature plants"),
    DEEP_MINER("deep_miner", "Deep Miner", 2, TargetGroup.PICKAXE, ChatColor.DARK_GRAY,
            "Grants Haste underground below Y=48"),
    ANGLER("angler", "Angler", 2, TargetGroup.FISHING_ROD, ChatColor.AQUA,
            "Has a 5% chance per level to catch one extra fish"),
    MARINER("mariner", "Mariner", 2, TargetGroup.FISHING_ROD, ChatColor.BLUE,
            "Adds one fishing experience per level on a successful catch"),
    TIDECALLER("tidecaller", "Tidecaller", 2, TargetGroup.TRIDENT, ChatColor.DARK_AQUA,
            "Deals 5% more damage to aquatic mobs per level"),
    SCAVENGER("scavenger", "Scavenger", 2, TargetGroup.MELEE_WEAPON, ChatColor.GOLD,
            "Has a 5% chance per level to yield one extra ordinary hostile-mob drop"),
    VOIDPROOF("voidproof", "Voidproof", 2, TargetGroup.ARMOR, ChatColor.DARK_PURPLE,
            "Reduces void damage by 10% per level; strongest worn piece counts"),
    AEGIS("aegis", "Aegis", 2, TargetGroup.SHIELD, ChatColor.LIGHT_PURPLE,
            "Reduces incoming damage by 8% per level while blocking");

    public enum TargetGroup {
        FISHING_ROD,
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        MINING_TOOL,
        TOOL_OR_WEAPON,
        MELEE_WEAPON,
        BOW_OR_CROSSBOW,
        TOOL,
        ARMOR,
        ANY_EQUIPMENT,
        SHIELD,
        TRIDENT,
        HELMET,
        CHESTPLATE,
        BOOTS
    }

    private static final NamespacedKey CUSTOM_GEAR_ID_KEY =
            new NamespacedKey("eaglercustomgear", "custom_gear_id");

    private final String key;
    private final String displayName;
    private final int maxLevel;
    private final TargetGroup targetGroup;
    private final ChatColor color;
    private final String description;

    EnchantmentType(String key, String displayName, int maxLevel, TargetGroup targetGroup, ChatColor color, String description) {
        this.key = key;
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.targetGroup = targetGroup;
        this.color = color;
        this.description = description;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public TargetGroup getTargetGroup() { return targetGroup; }
    public ChatColor getColor() { return color; }
    public String getDescription() { return description; }

    public boolean canApplyTo(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.getType() == Material.ENCHANTED_BOOK || item.getType() == Material.BOOK) return true;

        ItemMeta meta = item.getItemMeta();
        String customGearId = meta == null ? null
                : meta.getPersistentDataContainer().get(CUSTOM_GEAR_ID_KEY, PersistentDataType.STRING);
        return canApplyTo(item.getType(), customGearId);
    }

    boolean canApplyTo(Material material, String customGearId) {
        if (material == null || material == Material.AIR) return false;
        if (material == Material.ENCHANTED_BOOK || material == Material.BOOK) return true;

        String name = material.name();
        boolean isBoomerangDisk = material == Material.MUSIC_DISC_PIGSTEP
                && "boomerang_disk".equals(customGearId);
        boolean isPickaxe = name.endsWith("_PICKAXE");
        boolean isAxe = name.endsWith("_AXE");
        boolean isShovel = name.endsWith("_SHOVEL");
        boolean isHoe = name.endsWith("_HOE");
        boolean isSword = name.endsWith("_SWORD");
        boolean isArmor = name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
        boolean isTool = isPickaxe || isAxe || isShovel || isHoe || material == Material.FISHING_ROD;

        return switch (targetGroup) {
            case FISHING_ROD -> material == Material.FISHING_ROD;
            case PICKAXE -> isPickaxe;
            case AXE -> isAxe;
            case SHOVEL -> isShovel;
            case HOE -> isHoe;
            case MINING_TOOL -> isPickaxe || isShovel;
            case TOOL_OR_WEAPON -> isPickaxe || isAxe || isShovel || isHoe || isSword || material == Material.BOW || material == Material.CROSSBOW || material == Material.TRIDENT || isBoomerangDisk;
            case MELEE_WEAPON -> isSword || isAxe || material == Material.TRIDENT || material == Material.MACE
                    || isBoomerangDisk
                    || material == Material.NETHERITE_HOE;
            case BOW_OR_CROSSBOW -> material == Material.BOW || material == Material.CROSSBOW
                    || (isBoomerangDisk && (this == LONGSHOT || this == PINNING));
            case TOOL -> isTool;
            case ARMOR -> isArmor;
            case ANY_EQUIPMENT -> isArmor || isTool || isSword || material == Material.BOW || material == Material.CROSSBOW
                    || material == Material.TRIDENT || material == Material.MACE || material == Material.SHIELD || material == Material.ELYTRA
                    || isBoomerangDisk;
            case SHIELD -> material == Material.SHIELD;
            case TRIDENT -> material == Material.TRIDENT || (isBoomerangDisk && this == TIDECALLER);
            case HELMET -> name.endsWith("_HELMET") || material == Material.TURTLE_HELMET;
            case CHESTPLATE -> name.endsWith("_CHESTPLATE");
            case BOOTS -> name.endsWith("_BOOTS");
        };
    }

    public static EnchantmentType fromKey(String input) {
        for (EnchantmentType type : values()) {
            if (type.key.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}

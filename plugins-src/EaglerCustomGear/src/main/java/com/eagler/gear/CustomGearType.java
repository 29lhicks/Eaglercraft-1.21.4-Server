package com.eagler.gear;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

import java.util.List;

public enum CustomGearType {

    // ==================== 1. JUGGERNAUT ARMOR SET (Goliath) ====================
    JUGGERNAUT_HELMET("juggernaut_helmet", ChatColor.GOLD + "Juggernaut Helmet", Material.NETHERITE_HELMET,
            Category.ARMOR, "juggernaut", null, 4.0, 3.0, 0.0,
            List.of(ChatColor.GRAY + "Forged from Titan Heartstones.", ChatColor.YELLOW + "Full Set: +8 Hearts, Resistance I, Knockback Immune (-40% Speed)")),
    JUGGERNAUT_CHESTPLATE("juggernaut_chestplate", ChatColor.GOLD + "Juggernaut Chestplate", Material.NETHERITE_CHESTPLATE,
            Category.ARMOR, "juggernaut", null, 9.0, 4.0, 0.0,
            List.of(ChatColor.GRAY + "Near-impenetrable obsidian plating.", ChatColor.YELLOW + "Full Set: +8 Hearts, Resistance I, Knockback Immune (-40% Speed)")),
    JUGGERNAUT_LEGGINGS("juggernaut_leggings", ChatColor.GOLD + "Juggernaut Greaves", Material.NETHERITE_LEGGINGS,
            Category.ARMOR, "juggernaut", null, 7.0, 3.0, 0.0,
            List.of(ChatColor.GRAY + "Heavy plates anchored to the earth.", ChatColor.YELLOW + "Full Set: +8 Hearts, Resistance I, Knockback Immune (-40% Speed)")),
    JUGGERNAUT_BOOTS("juggernaut_boots", ChatColor.GOLD + "Juggernaut Sabatons", Material.NETHERITE_BOOTS,
            Category.ARMOR, "juggernaut", null, 4.0, 3.0, 0.0,
            List.of(ChatColor.GRAY + "Crushes stone beneath every step.", ChatColor.YELLOW + "Full Set: +8 Hearts, Resistance I, Knockback Immune (-40% Speed)")),

    // ==================== 2. ZEPHYR GALE ARMOR SET (Boreas) ====================
    ZEPHYR_HOOD("zephyr_hood", ChatColor.AQUA + "Zephyr Gale Hood", Material.LEATHER_HELMET,
            Category.ARMOR, "zephyr", Color.fromRGB(130, 230, 255), 3.0, 2.0, 0.0,
            List.of(ChatColor.GRAY + "Woven from condensed stormwinds.", ChatColor.AQUA + "Full Set: +52% Speed, Double-Jump, No Fall Dmg (-4 Hearts)")),
    ZEPHYR_TUNIC("zephyr_tunic", ChatColor.AQUA + "Zephyr Gale Tunic", Material.LEATHER_CHESTPLATE,
            Category.ARMOR, "zephyr", Color.fromRGB(100, 210, 255), 6.0, 2.0, 0.0,
            List.of(ChatColor.GRAY + "Featherlight tempest silk.", ChatColor.AQUA + "Full Set: +52% Speed, Double-Jump, No Fall Dmg (-4 Hearts)")),
    ZEPHYR_LEGGINGS("zephyr_leggings", ChatColor.AQUA + "Zephyr Gale Leggings", Material.LEATHER_LEGGINGS,
            Category.ARMOR, "zephyr", Color.fromRGB(130, 230, 255), 5.0, 2.0, 0.0,
            List.of(ChatColor.GRAY + "Channels upward gale drafts.", ChatColor.AQUA + "Full Set: +52% Speed, Double-Jump, No Fall Dmg (-4 Hearts)")),
    ZEPHYR_BOOTS("zephyr_boots", ChatColor.AQUA + "Zephyr Gale Boots", Material.LEATHER_BOOTS,
            Category.ARMOR, "zephyr", Color.fromRGB(180, 245, 255), 3.0, 2.0, 0.0,
            List.of(ChatColor.GRAY + "Tap jump mid-air to double-jump.", ChatColor.AQUA + "Full Set: +52% Speed, Double-Jump, No Fall Dmg (-4 Hearts)")),

    // ==================== 3. PYROMANCER ARMOR SET (Ignis) ====================
    PYRO_CROWN("pyro_crown", ChatColor.RED + "Pyromancer Crown", Material.GOLDEN_HELMET,
            Category.ARMOR, "pyro", null, 3.0, 2.5, 1.0,
            List.of(ChatColor.GRAY + "Smolders with eternal magma embers.", ChatColor.RED + "Full Set: +3 Hearts, +4 Dmg, Fire Immune & Igniting Strikes")),
    PYRO_CHESTPLATE("pyro_chestplate", ChatColor.RED + "Pyromancer Breastplate", Material.NETHERITE_CHESTPLATE,
            Category.ARMOR, "pyro", null, 8.0, 3.0, 1.5,
            List.of(ChatColor.GRAY + "Tempered in the heart of a volcano.", ChatColor.RED + "Full Set: +3 Hearts, +4 Dmg, Fire Immune & Igniting Strikes")),
    PYRO_LEGGINGS("pyro_leggings", ChatColor.RED + "Pyromancer Leggings", Material.GOLDEN_LEGGINGS,
            Category.ARMOR, "pyro", null, 6.0, 2.5, 1.0,
            List.of(ChatColor.GRAY + "Radiates scorching combat heat.", ChatColor.RED + "Full Set: +3 Hearts, +4 Dmg, Fire Immune & Igniting Strikes")),
    PYRO_BOOTS("pyro_boots", ChatColor.RED + "Pyromancer Treads", Material.GOLDEN_BOOTS,
            Category.ARMOR, "pyro", null, 3.0, 2.5, 0.5,
            List.of(ChatColor.GRAY + "Leaves a trail of harmless sparks.", ChatColor.RED + "Full Set: +3 Hearts, +4 Dmg, Fire Immune & Igniting Strikes")),

    // ==================== 4. SHADOWSTALKER ARMOR SET (Malakor) ====================
    SHADOW_MASK("shadow_mask", ChatColor.DARK_PURPLE + "Shadowstalker Mask", Material.LEATHER_HELMET,
            Category.ARMOR, "shadow", Color.fromRGB(35, 15, 55), 3.0, 2.5, 0.5,
            List.of(ChatColor.GRAY + "Shrouds your presence in void mist.", ChatColor.LIGHT_PURPLE + "Full Set: Sneak Invisibility & +50% Ambush Crit Dmg")),
    SHADOW_CLOAK("shadow_cloak", ChatColor.DARK_PURPLE + "Shadowstalker Cloak", Material.LEATHER_CHESTPLATE,
            Category.ARMOR, "shadow", Color.fromRGB(25, 10, 40), 7.0, 3.0, 1.0,
            List.of(ChatColor.GRAY + "Absorbs light while crouching.", ChatColor.LIGHT_PURPLE + "Full Set: Sneak Invisibility & +50% Ambush Crit Dmg")),
    SHADOW_LEGGINGS("shadow_leggings", ChatColor.DARK_PURPLE + "Shadowstalker Trousers", Material.LEATHER_LEGGINGS,
            Category.ARMOR, "shadow", Color.fromRGB(35, 15, 55), 6.0, 2.5, 0.5,
            List.of(ChatColor.GRAY + "Silences every footstep.", ChatColor.LIGHT_PURPLE + "Full Set: Sneak Invisibility & +50% Ambush Crit Dmg")),
    SHADOW_BOOTS("shadow_boots", ChatColor.DARK_PURPLE + "Shadowstalker Boots", Material.LEATHER_BOOTS,
            Category.ARMOR, "shadow", Color.fromRGB(20, 8, 35), 3.0, 2.5, 0.5,
            List.of(ChatColor.GRAY + "Swift movement through darkness.", ChatColor.LIGHT_PURPLE + "Full Set: Sneak Invisibility & +50% Ambush Crit Dmg")),

    // ==================== 5. CUSTOM WEAPONS & TOOLS ====================
    EXECUTIONER_GREATAXE("executioner_greataxe", ChatColor.RED + "" + ChatColor.BOLD + "Executioner's Greataxe", Material.NETHERITE_AXE,
            Category.WEAPON, null, null, 0.0, 0.0, 12.0,
            List.of(ChatColor.GRAY + "Heavy molten cleaver of Ignis.", ChatColor.GOLD + "Ability: +35% Bonus Damage to targets below 50% HP.")),
    VALKYRIE_SPEAR("valkyrie_spear", ChatColor.AQUA + "" + ChatColor.BOLD + "Valkyrie Storm Spear", Material.TRIDENT,
            Category.WEAPON, null, null, 0.0, 0.0, 10.0,
            List.of(ChatColor.GRAY + "Crackles with Boreas's lightning.", ChatColor.AQUA + "Ability: Right-click to dash forward in a gale burst (4s CD).")),
    SKYHOOK_ROD("skyhook_rod", ChatColor.AQUA + "" + ChatColor.BOLD + "Skyhook Grappling Rod", Material.FISHING_ROD,
            Category.TOOL, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Built-in aerial mobility grapple.", ChatColor.AQUA + "Ability: Launches you toward hooked blocks & negates fall damage.")),
    SOULREAVER_SCYTHE("soulreaver_scythe", ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Soulreaver Scythe", Material.NETHERITE_HOE,
            Category.WEAPON, null, null, 0.0, 0.0, 10.5,
            List.of(ChatColor.GRAY + "Harvests life force on every swing.", ChatColor.LIGHT_PURPLE + "Ability: Heals 1.5 Hearts on hit & cleaves nearby foes.")),
    PHANTOM_RAPIER("phantom_rapier", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Phantom Blink Rapier", Material.DIAMOND_SWORD,
            Category.WEAPON, null, null, 0.0, 0.0, 9.5,
            List.of(ChatColor.GRAY + "Phases through space in a blink.", ChatColor.LIGHT_PURPLE + "Ability: Right-click to shadow-step 6 blocks ahead (5s CD).")),
    EARTHSHAKER_PICKAXE("earthshaker_pickaxe", ChatColor.GOLD + "" + ChatColor.BOLD + "Earthshaker Hammer-Pick", Material.NETHERITE_PICKAXE,
            Category.TOOL, null, null, 0.0, 0.0, 7.0,
            List.of(ChatColor.GRAY + "Carved from a Titan Heartstone.", ChatColor.YELLOW + "Ability: Mines a 3x3 tunnel face (Sneak to mine single block).")),

    // ==================== 6. CUSTOM HEALTH & CONSUMABLE ITEMS ====================
    CRIMSON_HEART_FRUIT("crimson_heart_fruit", ChatColor.RED + "" + ChatColor.BOLD + "Crimson Heart Crystal", Material.APPLE,
            Category.HEALTH, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Crystallized vitality essence.", ChatColor.GREEN + "Right-Click: Permanently grants +1 Max Heart (up to +10 Bonus Hearts)!")),
    VITALITY_ELIXIR("vitality_elixir", ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Elixir of Immortality", Material.HONEY_BOTTLE,
            Category.HEALTH, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Brewed from golden apples & boss essence.", ChatColor.GREEN + "Drink: Instantly restores Full HP, Absorption IV & Regeneration II.")),
    BANDAGE_WRAP("bandage_wrap", ChatColor.GREEN + "" + ChatColor.BOLD + "Field Medic Bandage", Material.PAPER,
            Category.HEALTH, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Infused with healing herbs.", ChatColor.GREEN + "Right-Click: Instantly heals 4 Hearts (3s Cooldown).")),

    // ==================== 7. EARLY-GAME GEAR ====================
    TRAILBLAZER_HELMET("trailblazer_helmet", ChatColor.GREEN + "Trailblazer Hood", Material.LEATHER_HELMET,
            Category.ARMOR, "trailblazer", Color.fromRGB(105, 130, 75), 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Light leather armor reinforced with copper.", ChatColor.GREEN + "Full Set: +10% Movement Speed")),
    TRAILBLAZER_CHESTPLATE("trailblazer_chestplate", ChatColor.GREEN + "Trailblazer Tunic", Material.LEATHER_CHESTPLATE,
            Category.ARMOR, "trailblazer", Color.fromRGB(105, 130, 75), 1.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Light leather armor reinforced with copper.", ChatColor.GREEN + "Full Set: +10% Movement Speed")),
    TRAILBLAZER_LEGGINGS("trailblazer_leggings", ChatColor.GREEN + "Trailblazer Leggings", Material.LEATHER_LEGGINGS,
            Category.ARMOR, "trailblazer", Color.fromRGB(105, 130, 75), 1.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Light leather armor reinforced with copper.", ChatColor.GREEN + "Full Set: +10% Movement Speed")),
    TRAILBLAZER_BOOTS("trailblazer_boots", ChatColor.GREEN + "Trailblazer Boots", Material.LEATHER_BOOTS,
            Category.ARMOR, "trailblazer", Color.fromRGB(105, 130, 75), 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Light leather armor reinforced with copper.", ChatColor.GREEN + "Full Set: +10% Movement Speed")),
    IRONBOUND_HELMET("ironbound_helmet", ChatColor.GRAY + "Ironbound Helmet", Material.IRON_HELMET,
            Category.ARMOR, "ironbound", null, 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Iron armor strengthened with copper rivets.", ChatColor.YELLOW + "Full Set: +1 Heart")),
    IRONBOUND_CHESTPLATE("ironbound_chestplate", ChatColor.GRAY + "Ironbound Chestplate", Material.IRON_CHESTPLATE,
            Category.ARMOR, "ironbound", null, 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Iron armor strengthened with copper rivets.", ChatColor.YELLOW + "Full Set: +1 Heart")),
    IRONBOUND_LEGGINGS("ironbound_leggings", ChatColor.GRAY + "Ironbound Leggings", Material.IRON_LEGGINGS,
            Category.ARMOR, "ironbound", null, 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Iron armor strengthened with copper rivets.", ChatColor.YELLOW + "Full Set: +1 Heart")),
    IRONBOUND_BOOTS("ironbound_boots", ChatColor.GRAY + "Ironbound Boots", Material.IRON_BOOTS,
            Category.ARMOR, "ironbound", null, 0.5, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Iron armor strengthened with copper rivets.", ChatColor.YELLOW + "Full Set: +1 Heart")),
    COPPER_SHORTSWORD("copper_shortsword", ChatColor.GOLD + "Copperbound Shortsword", Material.STONE_SWORD,
            Category.WEAPON, null, null, 0.0, 0.0, 6.0,
            List.of(ChatColor.GRAY + "A simple stone blade with a copper edge.", ChatColor.YELLOW + "6 Attack Damage")),
    IRONBOUND_HATCHET("ironbound_hatchet", ChatColor.GRAY + "Ironbound Hatchet", Material.IRON_AXE,
            Category.WEAPON, null, null, 0.0, 0.0, 10.0,
            List.of(ChatColor.GRAY + "A heavy iron axe with a copper-bound haft.", ChatColor.YELLOW + "10 Attack Damage")),
    TRAILBLAZER_BOW("trailblazer_bow", ChatColor.GREEN + "Trailblazer Bow", Material.BOW,
            Category.WEAPON, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "A field bow tuned for accurate shots.", ChatColor.YELLOW + "Power I")),
    SKYBOUND_BOW("skybound_bow", ChatColor.AQUA + "" + ChatColor.BOLD + "Skybound Bow", Material.BOW,
            Category.WEAPON, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "Its arrows briefly lift the living.",
                    ChatColor.AQUA + "Ability: Arrow hits apply Levitation I for 2 seconds.")),
    BOOMERANG_DISK("boomerang_disk", ChatColor.GOLD + "" + ChatColor.BOLD + "Returning Disk", Material.MUSIC_DISC_PIGSTEP,
            Category.WEAPON, null, null, 0.0, 0.0, 0.0,
            List.of(ChatColor.GRAY + "A powerful disk that ricochets and returns to its thrower.",
                    ChatColor.YELLOW + "Throw damage: 8"));

    public enum Category {
        ARMOR, WEAPON, TOOL, HEALTH
    }

    private final String id;
    private final String displayName;
    private final Material material;
    private final Category category;
    private final String armorSet;
    private final Color leatherColor;
    private final double armorPoints;
    private final double armorToughness;
    private final double attackDamage;
    private final List<String> lore;

    CustomGearType(
            String id, String displayName, Material material, Category category,
            String armorSet, Color leatherColor, double armorPoints, double armorToughness,
            double attackDamage, List<String> lore
    ) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.category = category;
        this.armorSet = armorSet;
        this.leatherColor = leatherColor;
        this.armorPoints = armorPoints;
        this.armorToughness = armorToughness;
        this.attackDamage = attackDamage;
        this.lore = lore;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public Category getCategory() { return category; }
    public String getArmorSet() { return armorSet; }
    public Color getLeatherColor() { return leatherColor; }
    public double getArmorPoints() { return armorPoints; }
    public double getArmorToughness() { return armorToughness; }
    public double getAttackDamage() { return attackDamage; }
    public List<String> getLore() { return lore; }
    public boolean isBoomerangDisk() { return this == BOOMERANG_DISK; }

    public static CustomGearType fromId(String input) {
        for (CustomGearType type : values()) {
            if (type.id.equalsIgnoreCase(input) || type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}

package com.customrealms.relics.items;

import com.customrealms.relics.items.CustomItemDefinition.Category;
import com.customrealms.relics.items.CustomItemDefinition.Source;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class CustomItemRegistry {
    public static final String EVERLASTING_BAKED_POTATO_ID = "everlasting_baked_potato";
    private static final int EVERLASTING_BAKED_POTATO_USES = 64;

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey bakedPotatoUsesKey;
    private final Map<String, CustomItemDefinition> items = new LinkedHashMap<>();

    public CustomItemRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "custom_relic_id");
        this.bakedPotatoUsesKey = new NamespacedKey(plugin, "everlasting_baked_potato_uses");
    }

    public void register(CustomItemDefinition def) {
        items.put(def.getId(), def);
    }

    public void registerAll() {
        // ==================== 8 CUSTOM ARMOR PIECES ====================
        register(new CustomItemDefinition("titan_plate_helmet", "Titan Siege Helm", NamedTextColor.GOLD,
                Material.NETHERITE_HELMET, Category.ARMOR, Source.CRAFTABLE, 4101,
                List.of("+4 Max Health, +4 Armor, Knockback Resistance"),
                List.of("-12% Movement Speed"))
                .addAttr(Attribute.MAX_HEALTH, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.ARMOR, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.KNOCKBACK_RESISTANCE, 0.25, Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.12, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.HEAD));

        register(new CustomItemDefinition("titan_plate_chestplate", "Titan Siege Cuirass", NamedTextColor.GOLD,
                Material.NETHERITE_CHESTPLATE, Category.ARMOR, Source.CRAFTABLE, 4102,
                List.of("+6 Max Health, +9 Armor, Resistance I"),
                List.of("-18% Movement Speed, -10% Attack Speed"))
                .addAttr(Attribute.MAX_HEALTH, 6.0, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
                .addAttr(Attribute.ARMOR, 9.0, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.18, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.CHEST)
                .addAttr(Attribute.ATTACK_SPEED, -0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.CHEST));

        register(new CustomItemDefinition("zephyr_hood", "Zephyr Windrunner Hood", NamedTextColor.AQUA,
                Material.DIAMOND_HELMET, Category.ARMOR, Source.CRAFTABLE, 4103,
                List.of("+20% Movement Speed, Night Vision"),
                List.of("-3 Max Health"))
                .addAttr(Attribute.MOVEMENT_SPEED, 0.20, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.MAX_HEALTH, -3.0, Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));

        register(new CustomItemDefinition("zephyr_boots", "Zephyr Cloudstep Boots", NamedTextColor.AQUA,
                Material.DIAMOND_BOOTS, Category.ARMOR, Source.STRUCTURE_LOOT, 4104,
                List.of("+25% Speed, Jump Boost II, Safe Fall +4 Blocks"),
                List.of("-2 Armor Toughness"))
                .addAttr(Attribute.MOVEMENT_SPEED, 0.25, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.FEET)
                .addAttr(Attribute.ARMOR_TOUGHNESS, -2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.FEET)
                .addAttr(Attribute.SAFE_FALL_DISTANCE, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.FEET));

        register(new CustomItemDefinition("abyssal_chestplate", "Abyssal Warden Carapace", NamedTextColor.DARK_PURPLE,
                Material.NETHERITE_CHESTPLATE, Category.ARMOR, Source.BOSS_DROP, 4105,
                List.of("+8 Max Health, +10 Armor, Darkness Immunity"),
                List.of("Weakness I in bright daylight"))
                .addAttr(Attribute.MAX_HEALTH, 8.0, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
                .addAttr(Attribute.ARMOR, 10.0, Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));

        register(new CustomItemDefinition("solar_leggings", "Solaris Flame Greaves", NamedTextColor.YELLOW,
                Material.GOLDEN_LEGGINGS, Category.ARMOR, Source.STRUCTURE_LOOT, 4106,
                List.of("Permanent Fire Resistance, +15% Attack Damage"),
                List.of("Takes damage when submerged in water"))
                .addAttr(Attribute.ARMOR, 6.0, Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS)
                .addAttr(Attribute.ATTACK_DAMAGE, 0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.LEGS));

        register(new CustomItemDefinition("bloodforged_helm", "Bloodforged Berserker Visor", NamedTextColor.DARK_RED,
                Material.IRON_HELMET, Category.ARMOR, Source.CRAFTABLE, 4107,
                List.of("+25% Attack Damage, +15% Attack Speed"),
                List.of("-4 Max Health, Glowing outline"))
                .addAttr(Attribute.ATTACK_DAMAGE, 0.25, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.ATTACK_SPEED, 0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.HEAD)
                .addAttr(Attribute.MAX_HEALTH, -4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));

        register(new CustomItemDefinition("warden_greaves", "Sculk Resonance Leggings", NamedTextColor.DARK_AQUA,
                Material.NETHERITE_LEGGINGS, Category.ARMOR, Source.BOSS_DROP, 4108,
                List.of("+80% Sneak Speed, +6 Armor"),
                List.of("-10% Sprint Speed"))
                .addAttr(Attribute.SNEAKING_SPEED, 0.80, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.LEGS)
                .addAttr(Attribute.ARMOR, 6.0, Operation.ADD_NUMBER, EquipmentSlotGroup.LEGS));

        // ==================== 8 CUSTOM WEAPONS ====================
        register(new CustomItemDefinition("soul_reaver_scythe", "Soul Reaver Scythe", NamedTextColor.DARK_PURPLE,
                Material.NETHERITE_HOE, Category.WEAPON, Source.BOSS_DROP, 4201,
                List.of("11 Attack Damage, +1 Block Reach, Heals 2 HP on kill"),
                List.of("-25% Attack Speed, Drains 1 Hunger on miss"))
                .addAttr(Attribute.ATTACK_DAMAGE, 10.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ENTITY_INTERACTION_RANGE, 1.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ATTACK_SPEED, -2.6, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("chronos_rapier", "Chronos Quickblade", NamedTextColor.AQUA,
                Material.DIAMOND_SWORD, Category.WEAPON, Source.CRAFTABLE, 4202,
                List.of("Ultra-fast Attack Speed, +15% Move Speed while held"),
                List.of("Lower base damage (6.5)"))
                .addAttr(Attribute.ATTACK_DAMAGE, -0.5, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ATTACK_SPEED, 1.2, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, 0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("earthshatter_maul", "Earthshatter Greatmaul", NamedTextColor.GOLD,
                Material.MACE, Category.WEAPON, Source.CRAFTABLE, 4203,
                List.of("14 Attack Damage, vanilla fall smash, AoE Knockback Slam"),
                List.of("Very slow swing, -15% Movement Speed while held"))
                .addAttr(Attribute.ATTACK_DAMAGE, 8.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("cinderwake_mace", "Cinderwake Mace", NamedTextColor.RED,
                Material.MACE, Category.WEAPON, Source.CRAFTABLE, 4207,
                List.of("7 Attack Damage, vanilla fall smash"),
                List.of("Smash ignites nearby enemies"))
                .addAttr(Attribute.ATTACK_DAMAGE, 1.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("wardenfall_mace", "Wardenfall Mace", NamedTextColor.DARK_AQUA,
                Material.MACE, Category.WEAPON, Source.BOSS_DROP, 4208,
                List.of("14 Attack Damage, vanilla fall smash"),
                List.of("Smash adds a 6-damage quake and heals 2 HP"))
                .addAttr(Attribute.ATTACK_DAMAGE, 8.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("venomfang_dagger", "Venomfang Stiletto", NamedTextColor.DARK_GREEN,
                Material.IRON_SWORD, Category.WEAPON, Source.STRUCTURE_LOOT, 4204,
                List.of("Applies Poison II & Wither I on hit, Fast swing"),
                List.of("-2 Max Health while held"))
                .addAttr(Attribute.ATTACK_DAMAGE, 5.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ATTACK_SPEED, 1.5, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.MAX_HEALTH, -2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("starfall_longbow", "Starfall Astral Longbow", NamedTextColor.LIGHT_PURPLE,
                Material.BOW, Category.WEAPON, Source.STRUCTURE_LOOT, 4205,
                List.of("Arrows summon lightning on full-draw crit"),
                List.of("Applies Slowness I while aiming"))
                .addAttr(Attribute.MOVEMENT_SPEED, -0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("void_cleaver", "Voidrend Executioner", NamedTextColor.DARK_RED,
                Material.NETHERITE_SWORD, Category.WEAPON, Source.BOSS_DROP, 4206,
                List.of("12 Attack Damage, +50% Bonus Damage vs Targets <40% HP"),
                List.of("Inflicts 1 self-damage per swing"))
                .addAttr(Attribute.ATTACK_DAMAGE, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ATTACK_SPEED, -2.4, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // ==================== 5 CUSTOM TOOLS ====================
        register(new CustomItemDefinition("veinminer_pickaxe", "Worldcore Vein-Breaker", NamedTextColor.GOLD,
                Material.NETHERITE_PICKAXE, Category.TOOL, Source.CRAFTABLE, 4301,
                List.of("3x3 Stone/Ore Tunnel Mining, +2 Block Reach"),
                List.of("Consumes 2 Hunger per 3x3 break"))
                .addAttr(Attribute.BLOCK_INTERACTION_RANGE, 2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("treecapitator_axe", "Timberlord Felling Axe", NamedTextColor.GREEN,
                Material.DIAMOND_AXE, Category.TOOL, Source.CRAFTABLE, 4302,
                List.of("Fells connected vertical logs instantly"),
                List.of("Mining Fatigue I on non-wood blocks"))
                .addAttr(Attribute.BLOCK_BREAK_SPEED, 0.25, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("molten_shovel", "Magma-Core Excavator", NamedTextColor.RED,
                Material.NETHERITE_SHOVEL, Category.TOOL, Source.STRUCTURE_LOOT, 4303,
                List.of("Auto-smelts Sand -> Glass & Clay -> Terracotta"),
                List.of("-15% Movement Speed while held"))
                .addAttr(Attribute.MOVEMENT_SPEED, -0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("harvester_scythe", "Verdant Bountiful Hoe", NamedTextColor.GREEN,
                Material.DIAMOND_HOE, Category.TOOL, Source.CRAFTABLE, 4304,
                List.of("3x3 Auto-Replant Harvest + Bonus Crop Drops"),
                List.of("-20% Attack Damage"))
                .addAttr(Attribute.ATTACK_DAMAGE, -0.20, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND));

        register(new CustomItemDefinition("gravity_hammer", "Astral Terraformer", NamedTextColor.AQUA,
                Material.NETHERITE_PICKAXE, Category.TOOL, Source.BOSS_DROP, 4305,
                List.of("+40% Block Break Speed, +3 Block Reach, Haste II"),
                List.of("-4 Armor while held"))
                .addAttr(Attribute.BLOCK_BREAK_SPEED, 0.40, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.BLOCK_INTERACTION_RANGE, 3.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
                .addAttr(Attribute.ARMOR, -4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));

        // ==================== 10 OFF-HAND ARTIFACTS ====================
        register(new CustomItemDefinition("heart_of_the_abyss", "Heart of the Abyss", NamedTextColor.DARK_PURPLE,
                Material.ECHO_SHARD, Category.ARTIFACT, Source.BOSS_DROP, 4401,
                List.of("[Off-Hand] +8 Max Health, Regeneration I"),
                List.of("[Off-Hand] -20% Speed"))
                .addAttr(Attribute.MAX_HEALTH, 8.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.20, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("blood_chalice", "Crimson Blood Chalice", NamedTextColor.DARK_RED,
                Material.TOTEM_OF_UNDYING, Category.ARTIFACT, Source.STRUCTURE_LOOT, 4402,
                List.of("[Off-Hand] +30% Attack Damage, Lifesteal on hit"),
                List.of("[Off-Hand] -6 Max Health, Takes +15% Damage"))
                .addAttr(Attribute.ATTACK_DAMAGE, 0.30, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MAX_HEALTH, -6.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("chronos_hourglass", "Hourglass of Chronos", NamedTextColor.AQUA,
                Material.CLOCK, Category.ARTIFACT, Source.CRAFTABLE, 4403,
                List.of("[Off-Hand] +30% Movement Speed, +25% Attack Speed"),
                List.of("[Off-Hand] Hunger II drain"))
                .addAttr(Attribute.MOVEMENT_SPEED, 0.30, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.ATTACK_SPEED, 0.25, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("aegis_prism", "Aegis Bastion Prism", NamedTextColor.GOLD,
                Material.SHIELD, Category.ARTIFACT, Source.CRAFTABLE, 4404,
                List.of("[Off-Hand] +6 Armor, +4 Toughness, Resistance I"),
                List.of("[Off-Hand] -30% Attack Damage, -15% Speed"))
                .addAttr(Attribute.ARMOR, 6.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.ARMOR_TOUGHNESS, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.ATTACK_DAMAGE, -0.30, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("ember_totem", "Ignis Phoenix Idol", NamedTextColor.YELLOW,
                Material.BLAZE_POWDER, Category.ARTIFACT, Source.STRUCTURE_LOOT, 4405,
                List.of("[Off-Hand] Fire Immunity, Sets attackers on fire"),
                List.of("[Off-Hand] Slowness I & Weakness I in rain/water"))
                .addAttr(Attribute.ATTACK_DAMAGE, 2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("riftwalker_charm", "Riftwalker Charm", NamedTextColor.LIGHT_PURPLE,
                Material.ENDER_PEARL, Category.ARTIFACT, Source.CRAFTABLE, 4406,
                List.of("[Off-Hand] +10% Movement Speed, right-click to dash forward (15s cooldown)"),
                List.of())
                .addAttr(Attribute.MOVEMENT_SPEED, 0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("stormcaller_totem", "Stormcaller Totem", NamedTextColor.BLUE,
                Material.LIGHTNING_ROD, Category.ARTIFACT, Source.BOSS_DROP, 4407,
                List.of("[Off-Hand] +1 Attack Damage, right-click to strike up to 4 nearby enemies for 6 damage (25s cooldown)"),
                List.of("[Off-Hand] -10% Movement Speed"))
                .addAttr(Attribute.ATTACK_DAMAGE, 1.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("frostbound_idol", "Frostbound Idol", NamedTextColor.AQUA,
                Material.BLUE_ICE, Category.ARTIFACT, Source.STRUCTURE_LOOT, 4408,
                List.of("[Off-Hand] +2 Armor, right-click to freeze and slow nearby enemies (20s cooldown)"),
                List.of("[Off-Hand] -10% Attack Speed"))
                .addAttr(Attribute.ARMOR, 2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.ATTACK_SPEED, -0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("soulbloom_pendant", "Soulbloom Pendant", NamedTextColor.GREEN,
                Material.GOLDEN_APPLE, Category.ARTIFACT, Source.CRAFTABLE, 4409,
                List.of("[Off-Hand] +4 Max Health, right-click to heal 6 HP and cleanse ailments (30s cooldown)"),
                List.of("[Off-Hand] -10% Movement Speed"))
                .addAttr(Attribute.MAX_HEALTH, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.10, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition("bastion_heart", "Bastion Heart", NamedTextColor.GOLD,
                Material.HEART_OF_THE_SEA, Category.ARTIFACT, Source.BOSS_DROP, 4410,
                List.of("[Off-Hand] +4 Armor, +2 Toughness, right-click for Absorption II and Resistance I (40s cooldown)"),
                List.of("[Off-Hand] -15% Movement Speed"))
                .addAttr(Attribute.ARMOR, 4.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.ARMOR_TOUGHNESS, 2.0, Operation.ADD_NUMBER, EquipmentSlotGroup.OFFHAND)
                .addAttr(Attribute.MOVEMENT_SPEED, -0.15, Operation.MULTIPLY_SCALAR_1, EquipmentSlotGroup.OFFHAND));

        register(new CustomItemDefinition(EVERLASTING_BAKED_POTATO_ID, "Everlasting Baked Potato",
                NamedTextColor.GOLD, Material.BAKED_POTATO, Category.ARTIFACT, Source.STRUCTURE_LOOT, 4411,
                List.of("A baked potato that restores hunger. It never seems to run out."),
                List.of()));
    }

    public ItemStack createItem(String id) {
        CustomItemDefinition def = items.get(id);
        if (def == null) return null;

        ItemStack item = def.build(itemKey);
        if (EVERLASTING_BAKED_POTATO_ID.equals(id)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(1);
                meta.getPersistentDataContainer().set(
                        bakedPotatoUsesKey, PersistentDataType.INTEGER, EVERLASTING_BAKED_POTATO_USES);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public String getCustomId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
    }

    public CustomItemDefinition getDefinition(String id) {
        return items.get(id);
    }

    public int getEverlastingBakedPotatoUses(ItemStack item) {
        if (item == null || !EVERLASTING_BAKED_POTATO_ID.equals(getCustomId(item)) || !item.hasItemMeta()) {
            return 0;
        }
        Integer uses = item.getItemMeta().getPersistentDataContainer()
                .get(bakedPotatoUsesKey, PersistentDataType.INTEGER);
        return uses == null ? EVERLASTING_BAKED_POTATO_USES : uses;
    }

    public void setEverlastingBakedPotatoUses(ItemStack item, int uses) {
        if (item == null || !EVERLASTING_BAKED_POTATO_ID.equals(getCustomId(item))) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(bakedPotatoUsesKey, PersistentDataType.INTEGER, Math.max(0, uses));
        item.setItemMeta(meta);
    }

    public Collection<CustomItemDefinition> getAllItems() {
        return items.values();
    }

    public List<CustomItemDefinition> getBySource(Source source) {
        return items.values().stream().filter(i -> i.getSource() == source).collect(Collectors.toList());
    }
}

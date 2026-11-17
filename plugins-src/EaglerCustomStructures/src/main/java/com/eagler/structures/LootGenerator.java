package com.eagler.structures;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class LootGenerator {

    private record CustomEnchantDef(
            String key,
            String displayName,
            int maxLevel,
            ChatColor color,
            String description,
            Set<Material> compatibleTools
    ) {}

    private static final List<CustomEnchantDef> CUSTOM_ENCHANTS = List.of(
            new CustomEnchantDef("grapple", "Grapple", 2, ChatColor.AQUA, "Launches you toward your hooked bobber", Set.of(Material.FISHING_ROD)),
            new CustomEnchantDef("veinminer", "Veinminer", 3, ChatColor.GOLD, "Mines connected veins of matching ores", Set.of(Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE)),
            new CustomEnchantDef("auto_smelt", "Auto-Smelt", 1, ChatColor.RED, "Automatically smelts mined ores and blocks", Set.of(Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.IRON_SHOVEL)),
            new CustomEnchantDef("telekinesis", "Telekinesis", 1, ChatColor.LIGHT_PURPLE, "Sends block and mob drops directly to your inventory", Set.of(Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.IRON_SWORD, Material.DIAMOND_SWORD)),
            new CustomEnchantDef("timber", "Timber", 2, ChatColor.DARK_GREEN, "Fells entire trees from the bottom log", Set.of(Material.IRON_AXE, Material.DIAMOND_AXE)),
            new CustomEnchantDef("excavate", "Excavate", 2, ChatColor.YELLOW, "Digs a 3x3 area of soft blocks", Set.of(Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL)),
            new CustomEnchantDef("replenish", "Replenish", 1, ChatColor.GREEN, "Automatically replants harvested crops", Set.of(Material.IRON_HOE, Material.DIAMOND_HOE)),
            new CustomEnchantDef("experience", "Experience", 3, ChatColor.GREEN, "Increases EXP dropped from blocks and mobs", Set.of(Material.IRON_PICKAXE, Material.IRON_SWORD)),
            new CustomEnchantDef("lifesteal", "Lifesteal", 3, ChatColor.DARK_RED, "Heals a portion of melee damage dealt", Set.of(Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.IRON_AXE)),
            new CustomEnchantDef("thunderstrike", "Thunderstrike", 3, ChatColor.YELLOW, "Chance to strike targets with lightning", Set.of(Material.IRON_SWORD, Material.DIAMOND_SWORD)),
            new CustomEnchantDef("frostbite", "Frostbite", 2, ChatColor.AQUA, "Slows and freezes enemies on hit", Set.of(Material.IRON_SWORD, Material.DIAMOND_SWORD)),
            new CustomEnchantDef("poison_aspect", "Poison Aspect", 2, ChatColor.DARK_GREEN, "Inflicts poison on attacked targets", Set.of(Material.IRON_SWORD, Material.DIAMOND_SWORD)),
            new CustomEnchantDef("explosive_arrows", "Explosive Arrows", 2, ChatColor.RED, "Arrows detonate with a safe terrain-friendly blast", Set.of(Material.BOW, Material.CROSSBOW)),
            new CustomEnchantDef("glowing", "Glowing", 1, ChatColor.YELLOW, "Grants permanent Night Vision while worn", Set.of(Material.IRON_HELMET, Material.DIAMOND_HELMET)),
            new CustomEnchantDef("saturation", "Saturation", 3, ChatColor.GOLD, "Slowly restores hunger over time", Set.of(Material.IRON_HELMET, Material.DIAMOND_HELMET)),
            new CustomEnchantDef("molten", "Molten", 3, ChatColor.RED, "Ignites attackers who strike you", Set.of(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE)),
            new CustomEnchantDef("hardened", "Hardened", 3, ChatColor.GRAY, "Chance to negate incoming armor durability loss", Set.of(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE)),
            new CustomEnchantDef("gears", "Gears", 2, ChatColor.AQUA, "Grants permanent Speed while worn", Set.of(Material.IRON_BOOTS, Material.DIAMOND_BOOTS)),
            new CustomEnchantDef("springs", "Springs", 2, ChatColor.GREEN, "Grants permanent Jump Boost while worn", Set.of(Material.IRON_BOOTS, Material.DIAMOND_BOOTS)),
            new CustomEnchantDef("lava_walker", "Lava Walker", 2, ChatColor.DARK_RED, "Solidifies nearby lava into temporary magma blocks", Set.of(Material.IRON_BOOTS, Material.DIAMOND_BOOTS))
    );

    private final String enchantNamespace;
    private final Plugin plugin;
    private final Plugin customEnchantsPlugin;
    private final Plugin gearPlugin;
    private final Plugin relicsPlugin;
    private boolean warnedPotatoLookup;
    private boolean warnedEnchantLookup;

    public LootGenerator(Plugin plugin, Plugin customEnchantsPlugin, Plugin gearPlugin, Plugin relicsPlugin) {
        this.plugin = plugin;
        this.customEnchantsPlugin = customEnchantsPlugin;
        this.gearPlugin = gearPlugin;
        this.relicsPlugin = relicsPlugin;
        // Matches EaglerCustomEnchants's exact plugin namespace ("eaglercustomenchants")
        this.enchantNamespace = customEnchantsPlugin != null
                ? customEnchantsPlugin.getName().toLowerCase(Locale.ROOT)
                : "eaglercustomenchants";
    }

    public void populateChest(Inventory inv, StructureType structureType) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int itemCount = rng.nextInt(5, 10);
        List<Integer> emptySlots = new ArrayList<>();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (inv.getItem(slot) == null) {
                emptySlots.add(slot);
            }
        }

        int slotsToFill = Math.min(itemCount, emptySlots.size());
        for (int i = 0; i < slotsToFill; i++) {
            int slotIndex = rng.nextInt(emptySlots.size());
            int slot = emptySlots.remove(slotIndex);

            double roll = rng.nextDouble();
            if (roll < 0.32) {
                // 32%: Custom Enchanted Book from EaglerCustomEnchants
                inv.setItem(slot, createRandomCustomBook(rng));
            } else if (roll < 0.55) {
                // 23%: Gear enchanted with a Custom Enchantment (+ optional vanilla enchant)
                inv.setItem(slot, createCustomEnchantedGear(rng));
            } else if (roll < 0.72) {
                // 17%: Vanilla Enchanted Book
                inv.setItem(slot, createVanillaEnchantedBook(rng));
            } else {
                // 28%: Valuables & supplies tailored to structure theme
                inv.setItem(slot, createThemeSupply(structureType, rng));
            }
        }

        if (!emptySlots.isEmpty() && rng.nextDouble() < 0.10) {
            ItemStack potato = createEverlastingBakedPotato();
            if (potato != null) {
                inv.setItem(emptySlots.get(rng.nextInt(emptySlots.size())), potato);
            }
        }
    }

    private ItemStack createEverlastingBakedPotato() {
        if (relicsPlugin == null || !relicsPlugin.isEnabled()) return null;
        try {
            Object registry = relicsPlugin.getClass().getMethod("getItemRegistry").invoke(relicsPlugin);
            Object item = registry.getClass().getMethod("createItem", String.class)
                    .invoke(registry, "everlasting_baked_potato");
            if (item instanceof ItemStack stack) return stack;
            warnPotatoLookup("RelicsAndRuins did not return an Everlasting Baked Potato item.", null);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            warnPotatoLookup("Could not retrieve the Everlasting Baked Potato from RelicsAndRuins.", exception);
        }
        return null;
    }

    private void warnPotatoLookup(String message, Throwable exception) {
        if (warnedPotatoLookup) return;
        warnedPotatoLookup = true;
        if (exception == null) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().log(Level.WARNING, message, exception);
        }
    }

    private ItemStack createRandomCustomBook(ThreadLocalRandom rng) {
        if (customEnchantsPlugin != null && customEnchantsPlugin.isEnabled()) {
            try {
                Object manager = customEnchantsPlugin.getClass().getMethod("getEnchantmentManager")
                        .invoke(customEnchantsPlugin);
                Class<?> typeClass = Class.forName("com.eagler.enchants.EnchantmentType", true,
                        customEnchantsPlugin.getClass().getClassLoader());
                Object[] types = (Object[]) typeClass.getMethod("values").invoke(null);
                if (types.length > 0) {
                    Object type = types[rng.nextInt(types.length)];
                    int maxLevel = (int) typeClass.getMethod("getMaxLevel").invoke(type);
                    Object result = manager.getClass()
                            .getMethod("createEnchantedBook", typeClass, int.class)
                            .invoke(manager, type, rng.nextInt(1, maxLevel + 1));
                    if (result instanceof ItemStack book) {
                        return book;
                    }
                    warnEnchantLookup(null);
                }
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                warnEnchantLookup(exception);
            }
        }

        CustomEnchantDef def = CUSTOM_ENCHANTS.get(rng.nextInt(CUSTOM_ENCHANTS.size()));
        int level = rng.nextInt(1, def.maxLevel + 1);

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Custom Enchanted Book");
            NamespacedKey key = new NamespacedKey(enchantNamespace, "ce_" + def.key);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
            meta.setLore(List.of(
                    def.color + def.displayName + " " + toRoman(level),
                    ChatColor.DARK_GRAY + def.description
            ));
            meta.setEnchantmentGlintOverride(true);
            book.setItemMeta(meta);
        }
        return book;
    }

    private void warnEnchantLookup(Throwable exception) {
        if (warnedEnchantLookup) return;
        warnedEnchantLookup = true;
        String message = "Could not create a custom enchantment book through EaglerCustomEnchants; "
                + "using the built-in fallback pool.";
        if (exception == null) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().log(Level.WARNING, message, exception);
        }
    }

    private ItemStack createCustomEnchantedGear(ThreadLocalRandom rng) {
        if (gearPlugin != null) {
            ItemStack customGear = createCustomGear(rng);
            if (customGear != null) {
                addCustomEnchantment(customGear, rng);
                addVanillaEnchantment(customGear, rng);
                return customGear;
            }
        }

        CustomEnchantDef def = CUSTOM_ENCHANTS.get(rng.nextInt(CUSTOM_ENCHANTS.size()));
        List<Material> candidates = new ArrayList<>(def.compatibleTools);
        Material chosenMat = candidates.get(rng.nextInt(candidates.size()));

        ItemStack item = new ItemStack(chosenMat);
        int level = rng.nextInt(1, def.maxLevel + 1);

        // Optional vanilla enchantment alongside the custom enchantment
        if (rng.nextBoolean()) {
            if (chosenMat.name().endsWith("_SWORD")) {
                item.addUnsafeEnchantment(Enchantment.SHARPNESS, rng.nextInt(1, 4));
            } else if (chosenMat.name().endsWith("_PICKAXE") || chosenMat.name().endsWith("_SHOVEL") || chosenMat.name().endsWith("_AXE")) {
                item.addUnsafeEnchantment(Enchantment.EFFICIENCY, rng.nextInt(1, 4));
            } else if (chosenMat == Material.FISHING_ROD) {
                item.addUnsafeEnchantment(Enchantment.UNBREAKING, rng.nextInt(1, 4));
            } else if (chosenMat.name().endsWith("_HELMET") || chosenMat.name().endsWith("_CHESTPLATE") || chosenMat.name().endsWith("_BOOTS")) {
                item.addUnsafeEnchantment(Enchantment.PROTECTION, rng.nextInt(1, 4));
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey key = new NamespacedKey(enchantNamespace, "ce_" + def.key);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
            meta.setLore(List.of(def.color + def.displayName + " " + toRoman(level)));
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCustomGear(ThreadLocalRandom rng) {
        List<String> gearIds = List.of(
                "juggernaut_helmet", "juggernaut_chestplate", "juggernaut_leggings", "juggernaut_boots",
                "zephyr_hood", "zephyr_tunic", "zephyr_leggings", "zephyr_boots",
                "pyro_crown", "pyro_chestplate", "pyro_leggings", "pyro_boots",
                "shadow_mask", "shadow_cloak", "shadow_leggings", "shadow_boots",
                "executioner_greataxe", "valkyrie_spear", "skyhook_rod",
                "skybound_bow", "soulreaver_scythe", "phantom_rapier", "earthshaker_pickaxe",
                "boomerang_disk"
        );
        String gearId = gearIds.get(rng.nextInt(gearIds.size()));

        try {
            Object manager = gearPlugin.getClass().getMethod("getGearManager").invoke(gearPlugin);
            Class<?> typeClass = Class.forName("com.eagler.gear.CustomGearType", true,
                    gearPlugin.getClass().getClassLoader());
            Object type = typeClass.getMethod("fromId", String.class).invoke(null, gearId);
            if (type == null) {
                plugin.getLogger().warning("EaglerCustomGear does not recognize structure loot ID '" + gearId + "'.");
                return null;
            }
            Object result = manager.getClass().getMethod("createItem", typeClass).invoke(manager, type);
            if (result instanceof ItemStack item) {
                return item;
            }
            plugin.getLogger().warning("EaglerCustomGear returned an invalid item for structure loot ID '" + gearId + "'.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.WARNING, "Could not create custom gear for structure loot.", exception);
        }
        return null;
    }

    private void addCustomEnchantment(ItemStack item, ThreadLocalRandom rng) {
        Plugin customEnchants = plugin.getServer().getPluginManager().getPlugin("EaglerCustomEnchants");
        if (customEnchants == null) {
            return;
        }

        try {
            Object manager = customEnchants.getClass().getMethod("getEnchantmentManager").invoke(customEnchants);
            Class<?> typeClass = Class.forName("com.eagler.enchants.EnchantmentType", true,
                    customEnchants.getClass().getClassLoader());
            Method canApplyTo = typeClass.getMethod("canApplyTo", ItemStack.class);
            Method getMaxLevel = typeClass.getMethod("getMaxLevel");
            List<Object> compatible = new ArrayList<>();
            for (Object type : (Object[]) typeClass.getMethod("values").invoke(null)) {
                if ((boolean) canApplyTo.invoke(type, item)) {
                    compatible.add(type);
                }
            }
            if (compatible.isEmpty()) {
                plugin.getLogger().warning("No custom enchantment is compatible with structure loot "
                        + item.getType() + ".");
                return;
            }

            Object enchantment = compatible.get(rng.nextInt(compatible.size()));
            int maxLevel = (int) getMaxLevel.invoke(enchantment);
            manager.getClass().getMethod("addEnchantment", ItemStack.class, typeClass, int.class)
                    .invoke(manager, item, enchantment, rng.nextInt(1, maxLevel + 1));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.WARNING, "Could not add a custom enchantment to structure gear.", exception);
        }
    }

    private void addVanillaEnchantment(ItemStack item, ThreadLocalRandom rng) {
        if (!rng.nextBoolean()) {
            return;
        }

        Material material = item.getType();
        if ("boomerang_disk".equals(item.hasItemMeta()
                ? item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey("eaglercustomgear", "custom_gear_id"), PersistentDataType.STRING)
                : null)) {
            Enchantment[] pool = {
                    Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS,
                    Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.LOOTING
            };
            Enchantment enchantment = pool[rng.nextInt(pool.length)];
            item.addUnsafeEnchantment(enchantment, rng.nextInt(1, enchantment.getMaxLevel() + 1));
        } else if (material.name().endsWith("_SWORD")) {
            item.addUnsafeEnchantment(Enchantment.SHARPNESS, rng.nextInt(1, 4));
        } else if (material == Material.NETHERITE_HOE && item.hasItemMeta()
                && "soulreaver_scythe".equals(item.getItemMeta().getPersistentDataContainer().get(
                new NamespacedKey("eaglercustomgear", "custom_gear_id"), PersistentDataType.STRING))) {
            item.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
        } else if (material.name().endsWith("_PICKAXE") || material.name().endsWith("_SHOVEL")
                || material.name().endsWith("_AXE") || material.name().endsWith("_HOE")) {
            item.addUnsafeEnchantment(Enchantment.EFFICIENCY, rng.nextInt(1, 4));
        } else if (material == Material.FISHING_ROD || material == Material.TRIDENT) {
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, rng.nextInt(1, 4));
        } else if (material.name().endsWith("_HELMET") || material.name().endsWith("_CHESTPLATE")
                || material.name().endsWith("_LEGGINGS") || material.name().endsWith("_BOOTS")) {
            item.addUnsafeEnchantment(Enchantment.PROTECTION, rng.nextInt(1, 4));
        }
    }

    private ItemStack createVanillaEnchantedBook(ThreadLocalRandom rng) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (book.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            Enchantment[] pool = {
                    Enchantment.PROTECTION, Enchantment.SHARPNESS, Enchantment.EFFICIENCY,
                    Enchantment.UNBREAKING, Enchantment.FORTUNE, Enchantment.LOOTING, Enchantment.MENDING
            };
            Enchantment chosen = pool[rng.nextInt(pool.length)];
            int lvl = rng.nextInt(1, chosen.getMaxLevel() + 1);
            meta.addStoredEnchant(chosen, lvl, true);
            book.setItemMeta(meta);
        }
        return book;
    }

    private ItemStack createThemeSupply(StructureType type, ThreadLocalRandom rng) {
        return switch (type) {
            case BANDIT_WATCHTOWER -> switch (rng.nextInt(4)) {
                case 0 -> new ItemStack(Material.EMERALD, rng.nextInt(2, 7));
                case 1 -> new ItemStack(Material.IRON_INGOT, rng.nextInt(3, 9));
                case 2 -> new ItemStack(Material.GOLDEN_APPLE, 1);
                default -> new ItemStack(Material.ARROW, rng.nextInt(8, 24));
            };
            case MOSSY_SHRINE -> switch (rng.nextInt(4)) {
                case 0 -> new ItemStack(Material.DIAMOND, rng.nextInt(1, 4));
                case 1 -> new ItemStack(Material.EXPERIENCE_BOTTLE, rng.nextInt(3, 8));
                case 2 -> new ItemStack(Material.AMETHYST_SHARD, rng.nextInt(4, 12));
                default -> new ItemStack(Material.GOLDEN_CARROT, rng.nextInt(3, 8));
            };
            case DESERT_OBELISK -> switch (rng.nextInt(4)) {
                case 0 -> new ItemStack(Material.GOLD_INGOT, rng.nextInt(4, 12));
                case 1 -> new ItemStack(Material.LAPIS_LAZULI, rng.nextInt(4, 12));
                case 2 -> new ItemStack(Material.FIRE_CHARGE, rng.nextInt(2, 5));
                default -> new ItemStack(Material.SADDLE, 1);
            };
            case FROST_CAIRN -> switch (rng.nextInt(4)) {
                case 0 -> new ItemStack(Material.DIAMOND, rng.nextInt(1, 3));
                case 1 -> new ItemStack(Material.PACKED_ICE, rng.nextInt(6, 16));
                case 2 -> new ItemStack(Material.COOKED_BEEF, rng.nextInt(4, 10));
                default -> new ItemStack(Material.ENDER_PEARL, rng.nextInt(1, 4));
            };
            case SUNKEN_RUIN -> switch (rng.nextInt(4)) {
                case 0 -> new ItemStack(Material.PRISMARINE_CRYSTALS, rng.nextInt(3, 8));
                case 1 -> new ItemStack(Material.EMERALD, rng.nextInt(2, 6));
                case 2 -> new ItemStack(Material.NAUTILUS_SHELL, 1);
                default -> new ItemStack(Material.GOLD_NUGGET, rng.nextInt(8, 20));
            };
        };
    }

    private String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(number);
        };
    }
}

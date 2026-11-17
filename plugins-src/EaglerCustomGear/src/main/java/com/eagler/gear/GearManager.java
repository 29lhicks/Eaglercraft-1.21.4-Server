package com.eagler.gear;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class GearManager {

    private final JavaPlugin plugin;
    private final NamespacedKey gearIdKey;
    private final NamespacedKey bonusHeartsKey;
    private final NamespacedKey setHealthKey;
    private final NamespacedKey setSpeedKey;
    private final NamespacedKey setKnockbackKey;
    private final NamespacedKey setDamageKey;
    private final NamespacedKey bonusHealthModifierKey;
    private final Map<UUID, Boolean> flightBeforeZephyr = new HashMap<>();

    public GearManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.gearIdKey = new NamespacedKey(plugin, "custom_gear_id");
        this.bonusHeartsKey = new NamespacedKey(plugin, "permanent_bonus_hp");
        this.setHealthKey = new NamespacedKey(plugin, "set_bonus_health");
        this.setSpeedKey = new NamespacedKey(plugin, "set_bonus_speed");
        this.setKnockbackKey = new NamespacedKey(plugin, "set_bonus_knockback_resistance");
        this.setDamageKey = new NamespacedKey(plugin, "set_bonus_attack_damage");
        this.bonusHealthModifierKey = new NamespacedKey(plugin, "permanent_bonus_health");
    }

    public ItemStack createItem(CustomGearType type) {
        return createItem(type, 1);
    }

    public ItemStack createItem(CustomGearType type, int amount) {
        ItemStack item = new ItemStack(type.getMaterial(), Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(type.getDisplayName());
        meta.setLore(type.getLore());
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);
        meta.getPersistentDataContainer().set(gearIdKey, PersistentDataType.STRING, type.getId());

        if (type == CustomGearType.TRAILBLAZER_BOW) {
            meta.addEnchant(Enchantment.POWER, 1, true);
        }

        if (meta instanceof LeatherArmorMeta leatherMeta && type.getLeatherColor() != null) {
            leatherMeta.setColor(type.getLeatherColor());
        }

        // Apply 1.21.4 Attribute Modifiers
        if (type.getCategory() == CustomGearType.Category.ARMOR) {
            EquipmentSlotGroup slotGroup = getArmorSlotGroup(type.getMaterial());
            if (type.getArmorPoints() > 0) {
                meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                        new NamespacedKey(plugin, type.getId() + "_armor"),
                        type.getArmorPoints(), AttributeModifier.Operation.ADD_NUMBER, slotGroup));
            }
            if (type.getArmorToughness() > 0) {
                meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                        new NamespacedKey(plugin, type.getId() + "_toughness"),
                        type.getArmorToughness(), AttributeModifier.Operation.ADD_NUMBER, slotGroup));
            }
        } else if (type.getAttackDamage() > 0) {
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                    new NamespacedKey(plugin, type.getId() + "_dmg"),
                    getAttackDamageModifier(type), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        }

        item.setItemMeta(meta);
        return item;
    }

    static double getAttackDamageModifier(CustomGearType type) {
        double materialBonus = switch (type.getMaterial()) {
            case NETHERITE_AXE -> 9.0;
            case TRIDENT -> 8.0;
            case IRON_AXE -> 8.0;
            case STONE_SWORD -> 4.0;
            case DIAMOND_SWORD -> 6.0;
            case NETHERITE_PICKAXE -> 5.0;
            default -> 0.0;
        };
        return type.getAttackDamage() - 1.0 - materialBonus;
    }

    public CustomGearType identify(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(gearIdKey, PersistentDataType.STRING);
        return id != null ? CustomGearType.fromId(id) : null;
    }

    public String getEquippedFullSet(Player player) {
        CustomGearType helm = identify(player.getInventory().getHelmet());
        CustomGearType chest = identify(player.getInventory().getChestplate());
        CustomGearType legs = identify(player.getInventory().getLeggings());
        CustomGearType boots = identify(player.getInventory().getBoots());

        if (helm == null || chest == null || legs == null || boots == null) return null;
        String set = helm.getArmorSet();
        if (set != null && set.equals(chest.getArmorSet()) && set.equals(legs.getArmorSet()) && set.equals(boots.getArmorSet())) {
            return set;
        }
        return null;
    }

    public boolean hasZephyrBoots(Player player) {
        return grantsZephyrDoubleJump(identify(player.getInventory().getBoots()));
    }

    static boolean grantsZephyrDoubleJump(CustomGearType boots) {
        return boots == CustomGearType.ZEPHYR_BOOTS;
    }

    public double getPermanentBonusHp(Player player) {
        return player.getPersistentDataContainer().getOrDefault(bonusHeartsKey, PersistentDataType.DOUBLE, 0.0);
    }

    public boolean addPermanentBonusHp(Player player, double amount) {
        double current = getPermanentBonusHp(player);
        if (current >= 20.0) return false; // Cap at +10 Hearts (+20 HP)
        double updated = Math.min(20.0, current + amount);
        player.getPersistentDataContainer().set(bonusHeartsKey, PersistentDataType.DOUBLE, updated);
        updatePlayerAttributes(player, getEquippedFullSet(player));
        return true;
    }

    public void startArmorSetTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String activeSet = getEquippedFullSet(player);
                updatePlayerAttributes(player, activeSet);

                if ("juggernaut".equals(activeSet)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 50, 0, true, false, false));
                } else if ("pyro".equals(activeSet)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 50, 0, true, false, false));
                } else if ("shadow".equals(activeSet)) {
                    if (player.isSneaking()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30, 0, true, false, false));
                    }
                }
            }
        }, 20L, 20L);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                    continue;
                }

                if (hasZephyrBoots(player)) {
                    flightBeforeZephyr.putIfAbsent(player.getUniqueId(), player.getAllowFlight());
                    if (player.isOnGround() && !player.getAllowFlight()) {
                        player.setAllowFlight(true);
                    }
                } else {
                    restoreFlightPermission(player);
                }
            }
        }, 1L, 1L);
    }

    private void updatePlayerAttributes(Player player, String activeSet) {
        double setHpBonus = 0.0;
        double speedBonus = 0.0;
        double kbResist = 0.0;
        double dmgBonus = 0.0;

        if ("juggernaut".equals(activeSet)) {
            setHpBonus = 16.0; // +8 Hearts
            speedBonus = -0.04; // -40% Speed
            kbResist = 1.0; // Knockback Immune
        } else if ("zephyr".equals(activeSet)) {
            setHpBonus = -8.0; // -4 Hearts
            speedBonus = 0.052; // +52% Speed
        } else if ("pyro".equals(activeSet)) {
            setHpBonus = 6.0; // +3 Hearts
            dmgBonus = 4.0; // +4 Dmg
        } else if ("trailblazer".equals(activeSet)) {
            speedBonus = 0.01; // +10% movement speed
        } else if ("ironbound".equals(activeSet)) {
            setHpBonus = 2.0; // +1 Heart
        }

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        setModifier(maxHealth, setHealthKey, setHpBonus);
        setModifier(maxHealth, bonusHealthModifierKey, getPermanentBonusHp(player));
        setModifier(player.getAttribute(Attribute.MOVEMENT_SPEED), setSpeedKey, speedBonus);
        setModifier(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE), setKnockbackKey, kbResist);
        setModifier(player.getAttribute(Attribute.ATTACK_DAMAGE), setDamageKey, dmgBonus);

        if (maxHealth != null && player.getHealth() > maxHealth.getValue()) {
            player.setHealth(maxHealth.getValue());
        }
    }

    public void clearPlayerAttributes(Player player) {
        setModifier(player.getAttribute(Attribute.MAX_HEALTH), setHealthKey, 0.0);
        setModifier(player.getAttribute(Attribute.MAX_HEALTH), bonusHealthModifierKey, 0.0);
        setModifier(player.getAttribute(Attribute.MOVEMENT_SPEED), setSpeedKey, 0.0);
        setModifier(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE), setKnockbackKey, 0.0);
        setModifier(player.getAttribute(Attribute.ATTACK_DAMAGE), setDamageKey, 0.0);
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null && player.getHealth() > maxHealth.getValue()) {
            player.setHealth(maxHealth.getValue());
        }
        restoreFlightPermission(player);
    }

    private void restoreFlightPermission(Player player) {
        Boolean previouslyAllowed = flightBeforeZephyr.remove(player.getUniqueId());
        if (previouslyAllowed != null) {
            if (!previouslyAllowed && player.isFlying()) {
                player.setFlying(false);
            }
            player.setAllowFlight(previouslyAllowed);
        }
    }

    private void setModifier(AttributeInstance attribute, NamespacedKey key, double amount) {
        if (attribute == null) {
            return;
        }
        AttributeModifier current = attribute.getModifiers().stream()
                .filter(modifier -> modifier.getKey().equals(key))
                .findFirst()
                .orElse(null);

        if (Math.abs(amount) < 0.0001) {
            if (current != null) {
                attribute.removeModifier(current);
            }
            return;
        }
        if (current != null && Math.abs(current.getAmount() - amount) < 0.0001) {
            return;
        }
        if (current != null) {
            attribute.removeModifier(current);
        }
        attribute.addModifier(new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
    }

    ItemStack getBossMaterialIngredient(String matId, Material fallback) {
        ItemStack stack = new ItemStack(fallback);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String displayName;
            List<String> lore;
            switch (matId) {
                case "infernal_core" -> {
                    displayName = ChatColor.RED + "Infernal Core";
                    lore = List.of(
                            ChatColor.GRAY + "A blazing heart torn from Ignis or Cinder Knights.",
                            ChatColor.YELLOW + "Used to craft Infernal Summoning Crystals & high-tier gear."
                    );
                }
                case "tempest_essence" -> {
                    displayName = ChatColor.AQUA + "Tempest Essence";
                    lore = List.of(
                            ChatColor.GRAY + "Condensed gale energy dropped by Boreas & Gale Stalkers.",
                            ChatColor.YELLOW + "Used to craft Stormwing Summoning Crystals."
                    );
                }
                case "abyssal_soul_shard" -> {
                    displayName = ChatColor.DARK_PURPLE + "Abyssal Soul Shard";
                    lore = List.of(
                            ChatColor.GRAY + "A dark fragment harvested from Malakor & Void Acolytes.",
                            ChatColor.YELLOW + "Used to craft Void Reaper Summoning Crystals."
                    );
                }
                case "titan_heartstone" -> {
                    displayName = ChatColor.DARK_GRAY + "Titan Heartstone";
                    lore = List.of(
                            ChatColor.GRAY + "An indestructible core dropped by Goliath & Obsidian Sentinels.",
                            ChatColor.YELLOW + "Used to craft Colossus Summoning Crystals."
                    );
                }
                default -> throw new IllegalArgumentException("Unknown boss material ID: " + matId);
            }
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(
                    new NamespacedKey("eaglerbossesandbook", "custom_boss_material"),
                    PersistentDataType.STRING,
                    matId
            );
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void registerCraftingRecipes() {
        // Health Items
        ShapedRecipe bandage = new ShapedRecipe(new NamespacedKey(plugin, "craft_bandage"), createItem(CustomGearType.BANDAGE_WRAP, 3));
        bandage.shape(" P ", "PGP", " P ");
        bandage.setIngredient('P', Material.PAPER);
        bandage.setIngredient('G', Material.GLISTERING_MELON_SLICE);
        Bukkit.addRecipe(bandage);

        ShapedRecipe elixir = new ShapedRecipe(new NamespacedKey(plugin, "craft_elixir"), createItem(CustomGearType.VITALITY_ELIXIR, 1));
        elixir.shape(" G ", "GHG", " B ");
        elixir.setIngredient('G', Material.GOLDEN_APPLE);
        elixir.setIngredient('H', Material.HONEY_BOTTLE);
        elixir.setIngredient('B', Material.BLAZE_POWDER);
        Bukkit.addRecipe(elixir);

        ShapedRecipe heartCrystal = new ShapedRecipe(new NamespacedKey(plugin, "craft_heart_crystal"), createItem(CustomGearType.CRIMSON_HEART_FRUIT, 1));
        heartCrystal.shape("RGR", "GEG", "RGR");
        heartCrystal.setIngredient('R', Material.REDSTONE_BLOCK);
        heartCrystal.setIngredient('G', Material.GOLDEN_APPLE);
        heartCrystal.setIngredient('E', Material.EMERALD_BLOCK);
        Bukkit.addRecipe(heartCrystal);

        // Early-game gear uses only common materials and remains independent of boss loot.
        ShapedRecipe copperShortsword = new ShapedRecipe(
                new NamespacedKey(plugin, "craft_copper_shortsword"), createItem(CustomGearType.COPPER_SHORTSWORD));
        copperShortsword.shape(" C ", " C ", " S ");
        copperShortsword.setIngredient('C', Material.COPPER_INGOT);
        copperShortsword.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(copperShortsword);

        ShapedRecipe ironboundHatchet = new ShapedRecipe(
                new NamespacedKey(plugin, "craft_ironbound_hatchet"), createItem(CustomGearType.IRONBOUND_HATCHET));
        ironboundHatchet.shape("II ", "ISC", " S ");
        ironboundHatchet.setIngredient('I', Material.IRON_INGOT);
        ironboundHatchet.setIngredient('S', Material.STICK);
        ironboundHatchet.setIngredient('C', Material.COPPER_INGOT);
        Bukkit.addRecipe(ironboundHatchet);

        ShapedRecipe trailblazerBow = new ShapedRecipe(
                new NamespacedKey(plugin, "craft_trailblazer_bow"), createItem(CustomGearType.TRAILBLAZER_BOW));
        trailblazerBow.shape("SC ", "S T", "SC ");
        trailblazerBow.setIngredient('S', Material.STICK);
        trailblazerBow.setIngredient('C', Material.COPPER_INGOT);
        trailblazerBow.setIngredient('T', Material.STRING);
        Bukkit.addRecipe(trailblazerBow);

        registerBoomerangDiskRecipes();

        RecipeChoice copper = new RecipeChoice.MaterialChoice(Material.COPPER_INGOT);
        registerArmorSetRecipes(copper, Material.LEATHER,
                CustomGearType.TRAILBLAZER_HELMET, CustomGearType.TRAILBLAZER_CHESTPLATE,
                CustomGearType.TRAILBLAZER_LEGGINGS, CustomGearType.TRAILBLAZER_BOOTS);
        registerArmorSetRecipes(copper, Material.IRON_INGOT,
                CustomGearType.IRONBOUND_HELMET, CustomGearType.IRONBOUND_CHESTPLATE,
                CustomGearType.IRONBOUND_LEGGINGS, CustomGearType.IRONBOUND_BOOTS);

        // Weapons & Tools using Boss Materials (ExactChoice matches PDC boss material)
        RecipeChoice infernalCore = new RecipeChoice.ExactChoice(getBossMaterialIngredient("infernal_core", Material.MAGMA_CREAM));
        RecipeChoice tempestEssence = new RecipeChoice.ExactChoice(getBossMaterialIngredient("tempest_essence", Material.WIND_CHARGE));
        RecipeChoice soulShard = new RecipeChoice.ExactChoice(getBossMaterialIngredient("abyssal_soul_shard", Material.ECHO_SHARD));
        RecipeChoice titanHeart = new RecipeChoice.ExactChoice(getBossMaterialIngredient("titan_heartstone", Material.HEAVY_CORE));

        ShapedRecipe skyboundBow = new ShapedRecipe(
                new NamespacedKey(plugin, "craft_skybound_bow"), createItem(CustomGearType.SKYBOUND_BOW));
        skyboundBow.shape("FTF", "SBS", " S ");
        skyboundBow.setIngredient('F', Material.PHANTOM_MEMBRANE);
        skyboundBow.setIngredient('T', tempestEssence);
        skyboundBow.setIngredient('S', Material.STRING);
        skyboundBow.setIngredient('B', Material.BREEZE_ROD);
        Bukkit.addRecipe(skyboundBow);

        ShapedRecipe greataxe = new ShapedRecipe(new NamespacedKey(plugin, "craft_greataxe"), createItem(CustomGearType.EXECUTIONER_GREATAXE));
        greataxe.shape("II ", "IS ", " S ");
        greataxe.setIngredient('I', infernalCore);
        greataxe.setIngredient('S', Material.BLAZE_ROD);
        Bukkit.addRecipe(greataxe);

        ShapedRecipe spear = new ShapedRecipe(new NamespacedKey(plugin, "craft_spear"), createItem(CustomGearType.VALKYRIE_SPEAR));
        spear.shape(" TT", " ST", "S  ");
        spear.setIngredient('T', tempestEssence);
        spear.setIngredient('S', Material.BREEZE_ROD);
        Bukkit.addRecipe(spear);

        ShapedRecipe skyhook = new ShapedRecipe(new NamespacedKey(plugin, "craft_skyhook"), createItem(CustomGearType.SKYHOOK_ROD));
        skyhook.shape("  B", " BT", "S T");
        skyhook.setIngredient('B', Material.BREEZE_ROD);
        skyhook.setIngredient('T', tempestEssence);
        skyhook.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(skyhook);

        ShapedRecipe scythe = new ShapedRecipe(new NamespacedKey(plugin, "craft_scythe"), createItem(CustomGearType.SOULREAVER_SCYTHE));
        scythe.shape("AA ", " S ", " S ");
        scythe.setIngredient('A', soulShard);
        scythe.setIngredient('S', Material.NETHERITE_INGOT);
        Bukkit.addRecipe(scythe);

        ShapedRecipe rapier = new ShapedRecipe(new NamespacedKey(plugin, "craft_rapier"), createItem(CustomGearType.PHANTOM_RAPIER));
        rapier.shape("  A", " A ", "S  ");
        rapier.setIngredient('A', soulShard);
        rapier.setIngredient('S', Material.AMETHYST_SHARD);
        Bukkit.addRecipe(rapier);

        ShapedRecipe pickaxe = new ShapedRecipe(new NamespacedKey(plugin, "craft_earthshaker"), createItem(CustomGearType.EARTHSHAKER_PICKAXE));
        pickaxe.shape("TTT", " S ", " S ");
        pickaxe.setIngredient('T', titanHeart);
        pickaxe.setIngredient('S', Material.OBSIDIAN);
        Bukkit.addRecipe(pickaxe);

        // Register Armor Set Recipes (Boss Material + Base Armor Core)
        registerArmorSetRecipes(titanHeart, Material.NETHERITE_INGOT,
                CustomGearType.JUGGERNAUT_HELMET, CustomGearType.JUGGERNAUT_CHESTPLATE,
                CustomGearType.JUGGERNAUT_LEGGINGS, CustomGearType.JUGGERNAUT_BOOTS);

        registerArmorSetRecipes(tempestEssence, Material.PHANTOM_MEMBRANE,
                CustomGearType.ZEPHYR_HOOD, CustomGearType.ZEPHYR_TUNIC,
                CustomGearType.ZEPHYR_LEGGINGS, CustomGearType.ZEPHYR_BOOTS);

        registerArmorSetRecipes(infernalCore, Material.GOLD_INGOT,
                CustomGearType.PYRO_CROWN, CustomGearType.PYRO_CHESTPLATE,
                CustomGearType.PYRO_LEGGINGS, CustomGearType.PYRO_BOOTS);

        registerArmorSetRecipes(soulShard, Material.ECHO_SHARD,
                CustomGearType.SHADOW_MASK, CustomGearType.SHADOW_CLOAK,
                CustomGearType.SHADOW_LEGGINGS, CustomGearType.SHADOW_BOOTS);
    }

    private void registerBoomerangDiskRecipes() {
        ShapedRecipe disk = new ShapedRecipe(
                new NamespacedKey(plugin, "craft_boomerang_disk"),
                createItem(CustomGearType.BOOMERANG_DISK));
        disk.shape(" I ", "IDI", " I ");
        disk.setIngredient('I', Material.IRON_INGOT);
        disk.setIngredient('D', Material.DIAMOND);
        Bukkit.addRecipe(disk);
    }

    private void registerArmorSetRecipes(
            RecipeChoice coreMat, Material secondary,
            CustomGearType helmet, CustomGearType chest, CustomGearType legs, CustomGearType boots
    ) {
        ShapedRecipe h = new ShapedRecipe(new NamespacedKey(plugin, "craft_" + helmet.getId()), createItem(helmet));
        h.shape("CSC", "S S", "   ");
        h.setIngredient('C', coreMat);
        h.setIngredient('S', secondary);
        Bukkit.addRecipe(h);

        ShapedRecipe c = new ShapedRecipe(new NamespacedKey(plugin, "craft_" + chest.getId()), createItem(chest));
        c.shape("S S", "CSC", "SSS");
        c.setIngredient('C', coreMat);
        c.setIngredient('S', secondary);
        Bukkit.addRecipe(c);

        ShapedRecipe l = new ShapedRecipe(new NamespacedKey(plugin, "craft_" + legs.getId()), createItem(legs));
        l.shape("CSC", "S S", "S S");
        l.setIngredient('C', coreMat);
        l.setIngredient('S', secondary);
        Bukkit.addRecipe(l);

        ShapedRecipe b = new ShapedRecipe(new NamespacedKey(plugin, "craft_" + boots.getId()), createItem(boots));
        b.shape("   ", "C C", "S S");
        b.setIngredient('C', coreMat);
        b.setIngredient('S', secondary);
        Bukkit.addRecipe(b);
    }

    private EquipmentSlotGroup getArmorSlotGroup(Material mat) {
        String name = mat.name();
        if (name.endsWith("_HELMET")) return EquipmentSlotGroup.HEAD;
        if (name.endsWith("_CHESTPLATE")) return EquipmentSlotGroup.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlotGroup.LEGS;
        return EquipmentSlotGroup.FEET;
    }
}

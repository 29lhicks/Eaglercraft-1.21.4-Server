package com.eagler.bosses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ItemBridge {

    private final JavaPlugin plugin;
    private final NamespacedKey customMatKey;

    public ItemBridge(JavaPlugin plugin) {
        this.plugin = plugin;
        this.customMatKey = new NamespacedKey(plugin, "custom_boss_material");
    }

    public ItemStack createMaterial(CustomMaterialType type, int amount) {
        ItemStack item = new ItemStack(type.getMaterial(), Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(type.getDisplayName());
            meta.setLore(type.getLore());
            meta.setEnchantmentGlintOverride(true);
            meta.getPersistentDataContainer().set(customMatKey, PersistentDataType.STRING, type.getId());
            item.setItemMeta(meta);
        }
        return item;
    }

    public CustomMaterialType identifyMaterial(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(customMatKey, PersistentDataType.STRING);
        return id != null ? CustomMaterialType.fromId(id) : null;
    }

    public ItemStack createCustomGearById(String gearId) {
        Plugin gearPlugin = Bukkit.getPluginManager().getPlugin("EaglerCustomGear");
        if (gearPlugin == null) {
            plugin.getLogger().warning("EaglerCustomGear is unavailable; using a diamond fallback for gear reward '" + gearId + "'.");
            return new ItemStack(Material.DIAMOND, 3);
        }
        try {
            Method getGearManager = gearPlugin.getClass().getMethod("getGearManager");
            Object manager = getGearManager.invoke(gearPlugin);
            Class<?> enumClass = Class.forName("com.eagler.gear.CustomGearType", true, gearPlugin.getClass().getClassLoader());
            Method fromId = enumClass.getMethod("fromId", String.class);
            Object enumVal = fromId.invoke(null, gearId);
            if (enumVal != null) {
                Method createItem = manager.getClass().getMethod("createItem", enumClass);
                Object result = createItem.invoke(manager, enumVal);
                if (result instanceof ItemStack stack) {
                    return stack;
                }
            }
            plugin.getLogger().warning("EaglerCustomGear does not recognize gear reward ID '" + gearId + "'.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            plugin.getLogger().log(Level.WARNING, "Could not create EaglerCustomGear reward '" + gearId + "'.", exception);
        }
        return new ItemStack(Material.DIAMOND, 3);
    }

    public ItemStack createCustomEnchantedBook(String enchantKey, int level) {
        Plugin enchPlugin = Bukkit.getPluginManager().getPlugin("EaglerCustomEnchants");
        if (enchPlugin != null) {
            try {
                Method getEnchManager = enchPlugin.getClass().getMethod("getEnchantmentManager");
                Object manager = getEnchManager.invoke(enchPlugin);
                Class<?> enumClass = Class.forName("com.eagler.enchants.EnchantmentType", true, enchPlugin.getClass().getClassLoader());
                Method fromKey = enumClass.getMethod("fromKey", String.class);
                Object enumVal = fromKey.invoke(null, enchantKey);
                if (enumVal != null) {
                    Method createBook = manager.getClass().getMethod("createEnchantedBook", enumClass, int.class);
                    Object result = createBook.invoke(manager, enumVal, level);
                    if (result instanceof ItemStack stack) {
                        return stack;
                    }
                }
                plugin.getLogger().warning("EaglerCustomEnchants does not recognize enchantment key '" + enchantKey + "'. Using its compatible PDC format.");
            } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
                plugin.getLogger().log(Level.WARNING, "Could not create EaglerCustomEnchants book '" + enchantKey + "' through its API; using its compatible PDC format.", exception);
            }
        } else {
            plugin.getLogger().warning("EaglerCustomEnchants is unavailable; enchanted book reward '" + enchantKey + "' may not function until the plugin is installed.");
        }

        // Direct PDC fallback matching EaglerCustomEnchants
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            String pretty = enchantKey.substring(0, 1).toUpperCase(Locale.ROOT) + enchantKey.substring(1).replace('_', ' ');
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Custom Enchanted Book");
            meta.getPersistentDataContainer().set(new NamespacedKey("eaglercustomenchants", "ce_" + enchantKey), PersistentDataType.INTEGER, level);
            meta.setLore(List.of(ChatColor.AQUA + pretty + " " + (level == 2 ? "II" : "I")));
            meta.setEnchantmentGlintOverride(true);
            book.setItemMeta(meta);
        }
        return book;
    }
}

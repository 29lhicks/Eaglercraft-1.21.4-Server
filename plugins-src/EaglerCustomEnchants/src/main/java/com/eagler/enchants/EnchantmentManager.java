package com.eagler.enchants;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EnchantmentManager {

    private final JavaPlugin plugin;
    private final Map<EnchantmentType, NamespacedKey> keys = new EnumMap<>(EnchantmentType.class);

    public EnchantmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
        for (EnchantmentType type : EnchantmentType.values()) {
            keys.put(type, new NamespacedKey(plugin, "ce_" + type.getKey()));
        }
    }

    public NamespacedKey getKey(EnchantmentType type) {
        return keys.get(type);
    }

    public int getLevel(ItemStack item, EnchantmentType type) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(getKey(type), PersistentDataType.INTEGER, 0);
    }

    public Map<EnchantmentType, Integer> getEnchantments(ItemStack item) {
        Map<EnchantmentType, Integer> map = new LinkedHashMap<>();
        if (item == null || !item.hasItemMeta()) return map;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (EnchantmentType type : EnchantmentType.values()) {
            int lvl = pdc.getOrDefault(getKey(type), PersistentDataType.INTEGER, 0);
            if (lvl > 0) {
                map.put(type, lvl);
            }
        }
        return map;
    }

    public void addEnchantment(ItemStack item, EnchantmentType type, int level) {
        if (item == null || item.getType() == Material.AIR) return;
        int clamped = Math.max(1, Math.min(type.getMaxLevel(), level));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(getKey(type), PersistentDataType.INTEGER, clamped);
        item.setItemMeta(meta);
        refreshLoreAndGlint(item);
    }

    public ItemStack createEnchantedBook(EnchantmentType type, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Custom Enchanted Book");
            book.setItemMeta(meta);
        }
        addEnchantment(book, type, level);
        return book;
    }

    public void refreshLoreAndGlint(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Map<EnchantmentType, Integer> active = getEnchantments(item);

        List<String> newLore = new ArrayList<>();
        for (Map.Entry<EnchantmentType, Integer> entry : active.entrySet()) {
            EnchantmentType type = entry.getKey();
            String numeral = toRoman(entry.getValue());
            newLore.add(type.getColor() + type.getDisplayName() + " " + numeral);
        }

        if (item.getType() == Material.ENCHANTED_BOOK && active.size() == 1) {
            EnchantmentType only = active.keySet().iterator().next();
            newLore.add(ChatColor.DARK_GRAY + only.getDescription());
        }

        // Preserve non-enchantment custom lore lines
        if (meta.hasLore() && meta.getLore() != null) {
            for (String line : meta.getLore()) {
                String stripped = ChatColor.stripColor(line);
                boolean isManaged = false;
                for (EnchantmentType t : EnchantmentType.values()) {
                    if (stripped.startsWith(t.getDisplayName() + " ") || stripped.equals(t.getDescription())) {
                        isManaged = true;
                        break;
                    }
                }
                if (!isManaged) {
                    newLore.add(line);
                }
            }
        }

        meta.setLore(newLore.isEmpty() ? null : newLore);
        if (!active.isEmpty()) {
            meta.setEnchantmentGlintOverride(true);
        }
        item.setItemMeta(meta);
    }

    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }
}

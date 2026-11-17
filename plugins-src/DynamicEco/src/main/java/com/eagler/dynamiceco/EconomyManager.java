package com.eagler.dynamiceco;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class EconomyManager {
    private final DynamicEco plugin;
    private final File balanceFile;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final Map<Material, Double> basePrices = new HashMap<>();
    private final Map<Material, Double> currentPrices = new HashMap<>();
    private final Map<String, Double> customBasePrices = new HashMap<>();
    private final Map<String, Double> customCurrentPrices = new HashMap<>();
    private final NamespacedKey customGearKey = new NamespacedKey("eaglercustomgear", "custom_gear_id");
    private final NamespacedKey customBossMaterialKey =
            new NamespacedKey("eaglerbossesandbook", "custom_boss_material");
    private FileConfiguration balanceConfig;

    EconomyManager(DynamicEco plugin) {
        this.plugin = plugin;
        balanceFile = new File(plugin.getDataFolder(), "balances.yml");
        loadData();
    }

    private void loadData() {
        if (!balanceFile.exists()) {
            try {
                if (!balanceFile.createNewFile()) {
                    plugin.getLogger().warning("balances.yml already exists; loading it.");
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not create balances.yml", e);
            }
        }

        balanceConfig = YamlConfiguration.loadConfiguration(balanceFile);
        for (String key : balanceConfig.getKeys(false)) {
            try {
                double balance = balanceConfig.getDouble(key);
                if (Double.isFinite(balance) && balance >= 0.0) {
                    balances.put(UUID.fromString(key), round(balance));
                } else {
                    plugin.getLogger().warning("Ignoring invalid balance for " + key);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Ignoring invalid player UUID in balances.yml: " + key);
            }
        }

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("items");
        if (items == null) {
            plugin.getLogger().warning("No market items are configured.");
        } else {
            for (String key : items.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                double base = items.getDouble(key + ".base-price", -1.0);
                double current = items.getDouble(key + ".current-price", base);
                if (material == null || !Double.isFinite(base) || base <= 0.0
                        || !Double.isFinite(current) || current <= 0.0) {
                    plugin.getLogger().warning("Ignoring invalid market item configuration: " + key);
                    continue;
                }

                basePrices.put(material, base);
                currentPrices.put(material, clampPrice(material, current));
            }
        }

        ConfigurationSection customItems = plugin.getConfig().getConfigurationSection("custom-items");
        if (customItems == null) {
            return;
        }
        for (String category : customItems.getKeys(false)) {
            ConfigurationSection categoryItems = customItems.getConfigurationSection(category);
            if (categoryItems == null) {
                continue;
            }
            for (String id : categoryItems.getKeys(false)) {
                String key = category + "." + id;
                double base = categoryItems.getDouble(id + ".base-price", -1.0);
                double current = categoryItems.getDouble(id + ".current-price", base);
                if (!Double.isFinite(base) || base <= 0.0 || !Double.isFinite(current) || current <= 0.0) {
                    plugin.getLogger().warning("Ignoring invalid custom market item configuration: " + key);
                    continue;
                }
                customBasePrices.put(key, base);
                customCurrentPrices.put(key, clampCustomPrice(key, current));
            }
        }
    }

    void saveData() {
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            balanceConfig.set(entry.getKey().toString(), round(entry.getValue()));
        }
        try {
            balanceConfig.save(balanceFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save balances.yml: " + e.getMessage());
        }

        for (Map.Entry<Material, Double> entry : currentPrices.entrySet()) {
            plugin.getConfig().set("items." + entry.getKey().name() + ".current-price", round(entry.getValue()));
        }
        for (Map.Entry<String, Double> entry : customCurrentPrices.entrySet()) {
            plugin.getConfig().set("custom-items." + entry.getKey() + ".current-price", round(entry.getValue()));
        }
        plugin.saveConfig();
    }

    double getBalance(UUID playerId) {
        return balances.computeIfAbsent(playerId, ignored ->
                validNonNegative(plugin.getConfig().getDouble("starting-balance", 500.0), 500.0));
    }

    boolean deposit(UUID playerId, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            return false;
        }
        double updated = getBalance(playerId) + amount;
        if (!Double.isFinite(updated)) {
            return false;
        }
        balances.put(playerId, round(updated));
        return true;
    }

    boolean withdraw(UUID playerId, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0) {
            return false;
        }
        double balance = getBalance(playerId);
        if (balance < amount) {
            return false;
        }
        balances.put(playerId, round(balance - amount));
        return true;
    }

    boolean isMarketItem(Material material) {
        return currentPrices.containsKey(material);
    }

    Set<Material> getMarketItems() {
        return Set.copyOf(currentPrices.keySet());
    }

    double getBasePrice(Material material) {
        return basePrices.getOrDefault(material, 0.0);
    }

    double getBuyPrice(Material material) {
        return currentPrices.getOrDefault(material, 0.0);
    }

    double getSellPrice(Material material) {
        return round(getBuyPrice(material) * getSellRatio());
    }

    String getCustomMarketKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String gearId = item.getItemMeta().getPersistentDataContainer()
                .get(customGearKey, PersistentDataType.STRING);
        if (gearId != null) {
            return "gear." + gearId;
        }
        String bossMaterialId = item.getItemMeta().getPersistentDataContainer()
                .get(customBossMaterialKey, PersistentDataType.STRING);
        if (bossMaterialId != null) {
            return "boss-material." + bossMaterialId;
        }
        return null;
    }

    boolean isCustomMarketItem(String key) {
        return key != null && customCurrentPrices.containsKey(key);
    }

    double getCustomBuyPrice(String key) {
        return customCurrentPrices.getOrDefault(key, 0.0);
    }

    double getCustomSellPrice(String key) {
        return round(getCustomBuyPrice(key) * getSellRatio());
    }

    void recordCustomSale(String key, int quantity) {
        if (!isCustomMarketItem(key) || quantity <= 0) {
            return;
        }
        double updated = getCustomBuyPrice(key) * Math.pow(1.0 - getVolatility(), quantity);
        customCurrentPrices.put(key, clampCustomPrice(key, updated));
    }

    void recordPurchase(Material material, int quantity) {
        if (!isMarketItem(material) || quantity <= 0) {
            return;
        }
        double volatility = getVolatility();
        double updated = getBuyPrice(material) * Math.pow(1.0 + volatility, quantity);
        currentPrices.put(material, clampPrice(material, updated));
    }

    void recordSale(Material material, int quantity) {
        if (!isMarketItem(material) || quantity <= 0) {
            return;
        }
        double updated = getBuyPrice(material) * Math.pow(1.0 - getVolatility(), quantity);
        currentPrices.put(material, clampPrice(material, updated));
    }

    void stabilizeMarket() {
        double rate = plugin.getConfig().getDouble("stabilize-rate", 0.05);
        if (!Double.isFinite(rate)) {
            rate = 0.05;
        }
        rate = Math.max(0.0, Math.min(1.0, rate));

        for (Material material : currentPrices.keySet()) {
            double base = basePrices.get(material);
            double current = currentPrices.get(material);
            currentPrices.put(material, clampPrice(material, current + (base - current) * rate));
        }
        for (String key : customCurrentPrices.keySet()) {
            double base = customBasePrices.get(key);
            double current = customCurrentPrices.get(key);
            customCurrentPrices.put(key, clampCustomPrice(key, current + (base - current) * rate));
        }
    }

    double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double getVolatility() {
        double volatility = plugin.getConfig().getDouble("volatility", 0.01);
        if (!Double.isFinite(volatility)) {
            return 0.01;
        }
        return Math.max(0.0, Math.min(1.0, volatility));
    }

    private double clampPrice(Material material, double price) {
        double base = basePrices.getOrDefault(material,
                plugin.getConfig().getDouble("items." + material.name() + ".base-price", 1.0));
        double minMultiplier = validMultiplier("min-multiplier", 0.2);
        double maxMultiplier = validMultiplier("max-multiplier", 5.0);
        double min = base * minMultiplier;
        double max = base * Math.max(minMultiplier, maxMultiplier);
        if (!Double.isFinite(price)) {
            return max;
        }
        return round(Math.max(min, Math.min(max, price)));
    }

    private double clampCustomPrice(String key, double price) {
        double base = customBasePrices.get(key);
        double minMultiplier = validMultiplier("min-multiplier", 0.2);
        double maxMultiplier = validMultiplier("max-multiplier", 5.0);
        double min = base * minMultiplier;
        double max = base * Math.max(minMultiplier, maxMultiplier);
        if (!Double.isFinite(price)) {
            return max;
        }
        return round(Math.max(min, Math.min(max, price)));
    }

    private double getSellRatio() {
        double ratio = plugin.getConfig().getDouble("sell-ratio", 0.8);
        if (!Double.isFinite(ratio)) {
            return 0.8;
        }
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    private double validMultiplier(String path, double fallback) {
        double value = plugin.getConfig().getDouble(path, fallback);
        return Double.isFinite(value) && value > 0.0 ? value : fallback;
    }

    private double validNonNegative(double value, double fallback) {
        return Double.isFinite(value) && value >= 0.0 ? value : fallback;
    }
}

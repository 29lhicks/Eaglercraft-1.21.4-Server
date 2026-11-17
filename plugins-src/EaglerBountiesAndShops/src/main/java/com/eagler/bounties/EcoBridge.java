package com.eagler.bounties;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

public class EcoBridge {

    private final JavaPlugin plugin;

    public EcoBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private Object getDynamicEcoManager() {
        Plugin ecoPlugin = requirePlugin("DynamicEco");
        try {
            Field field = ecoPlugin.getClass().getDeclaredField("economyManager");
            field.setAccessible(true);
            Object manager = field.get(ecoPlugin);
            if (manager == null) {
                throw new IllegalStateException("DynamicEco economy manager is not initialized.");
            }
            return manager;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            throw integrationFailure("DynamicEco economy manager", exception);
        }
    }

    public double getBalance(UUID uuid) {
        Object result = invoke(getDynamicEcoManager(), "getBalance", new Class<?>[]{UUID.class}, uuid);
        if (result instanceof Number balance) {
            return balance.doubleValue();
        }
        throw new IllegalStateException("DynamicEco returned an invalid player balance.");
    }

    public boolean deposit(UUID uuid, double amount) {
        Object result = invoke(getDynamicEcoManager(), "deposit", new Class<?>[]{UUID.class, double.class}, uuid, amount);
        if (result instanceof Boolean deposited) {
            return deposited;
        }
        throw new IllegalStateException("DynamicEco returned an invalid deposit result.");
    }

    public boolean withdraw(UUID uuid, double amount) {
        Object result = invoke(getDynamicEcoManager(), "withdraw", new Class<?>[]{UUID.class, double.class}, uuid, amount);
        if (result instanceof Boolean withdrawn) {
            return withdrawn;
        }
        throw new IllegalStateException("DynamicEco returned an invalid withdrawal result.");
    }

    public ItemStack createBossMaterial(String matId, int amount) {
        Plugin bossPlugin = requirePlugin("EaglerBossesAndBook");
        try {
            Field bridgeField = bossPlugin.getClass().getDeclaredField("itemBridge");
            bridgeField.setAccessible(true);
            Object bridge = bridgeField.get(bossPlugin);
            Class<?> enumClass = Class.forName("com.eagler.bosses.CustomMaterialType", true, bossPlugin.getClass().getClassLoader());
            Object enumVal = enumClass.getMethod("fromId", String.class).invoke(null, matId);
            if (enumVal == null) {
                throw new IllegalArgumentException("Unknown boss material ID: " + matId);
            }
            return requireItem(invoke(bridge, "createMaterial", new Class<?>[]{enumClass, int.class}, enumVal, amount),
                    "EaglerBossesAndBook material " + matId);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            throw integrationFailure("EaglerBossesAndBook material " + matId, exception);
        }
    }

    public ItemStack createCustomGear(String gearId, int amount) {
        Plugin gearPlugin = requirePlugin("EaglerCustomGear");
        try {
            Object manager = invoke(gearPlugin, "getGearManager", new Class<?>[0]);
            Class<?> enumClass = Class.forName("com.eagler.gear.CustomGearType", true, gearPlugin.getClass().getClassLoader());
            Object enumVal = enumClass.getMethod("fromId", String.class).invoke(null, gearId);
            if (enumVal == null) {
                throw new IllegalArgumentException("Unknown custom gear ID: " + gearId);
            }
            return requireItem(invoke(manager, "createItem", new Class<?>[]{enumClass, int.class}, enumVal, amount),
                    "EaglerCustomGear item " + gearId);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            throw integrationFailure("EaglerCustomGear item " + gearId, exception);
        }
    }

    public ItemStack createCustomBook(String enchantKey, int level) {
        Plugin enchPlugin = requirePlugin("EaglerCustomEnchants");
        try {
            Object manager = invoke(enchPlugin, "getEnchantmentManager", new Class<?>[0]);
            Class<?> enumClass = Class.forName("com.eagler.enchants.EnchantmentType", true, enchPlugin.getClass().getClassLoader());
            Object enumVal = enumClass.getMethod("fromKey", String.class).invoke(null, enchantKey);
            if (enumVal == null) {
                throw new IllegalArgumentException("Unknown custom enchantment key: " + enchantKey);
            }
            return requireItem(invoke(manager, "createEnchantedBook", new Class<?>[]{enumClass, int.class}, enumVal, level),
                    "EaglerCustomEnchants book " + enchantKey);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            throw integrationFailure("EaglerCustomEnchants book " + enchantKey, exception);
        }
    }

    private Plugin requirePlugin(String name) {
        Plugin dependency = Bukkit.getPluginManager().getPlugin(name);
        if (dependency == null || !dependency.isEnabled()) {
            throw new IllegalStateException("Required plugin is not enabled: " + name);
        }
        return dependency;
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... arguments) {
        if (target == null) {
            throw new IllegalStateException("Cannot invoke " + methodName + " on an uninitialized plugin API.");
        }
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            throw integrationFailure(target.getClass().getName() + "." + methodName, exception);
        }
    }

    private ItemStack requireItem(Object item, String source) {
        if (item instanceof ItemStack stack) {
            return stack;
        }
        throw new IllegalStateException(source + " did not return an item.");
    }

    private IllegalStateException integrationFailure(String integration, Throwable cause) {
        plugin.getLogger().log(Level.SEVERE, "Could not use " + integration + ".", cause);
        return new IllegalStateException("Could not use " + integration + ".", cause);
    }

    public String getBossOrEliteId(org.bukkit.entity.Entity entity) {
        String bossId = entity.getPersistentDataContainer().get(new NamespacedKey("eaglerbossesandbook", "custom_boss_id"), PersistentDataType.STRING);
        if (bossId != null) return "boss:" + bossId;
        String eliteId = entity.getPersistentDataContainer().get(new NamespacedKey("eaglerbossesandbook", "custom_elite_id"), PersistentDataType.STRING);
        if (eliteId != null) return "elite:" + eliteId;
        return null;
    }
}

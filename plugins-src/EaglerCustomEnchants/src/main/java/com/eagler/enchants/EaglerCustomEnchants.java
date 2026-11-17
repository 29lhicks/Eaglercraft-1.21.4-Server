package com.eagler.enchants;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EaglerCustomEnchants extends JavaPlugin {

    private EnchantmentManager enchantmentManager;
    private GameplayListener gameplayListener;

    @Override
    public void onEnable() {
        this.enchantmentManager = new EnchantmentManager(this);

        getServer().getPluginManager().registerEvents(new DistributionListener(this, enchantmentManager), this);
        this.gameplayListener = new GameplayListener(this, enchantmentManager);
        getServer().getPluginManager().registerEvents(gameplayListener, this);

        EnchantCommand cmd = new EnchantCommand(enchantmentManager);
        if (getCommand("ce") != null) {
            getCommand("ce").setExecutor(cmd);
            getCommand("ce").setTabCompleter(cmd);
        }

        // Passive armor task (runs every 2 seconds)
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Glowing (Helmet)
                int glowing = enchantmentManager.getLevel(player.getInventory().getHelmet(), EnchantmentType.GLOWING);
                if (glowing > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 260, 0, true, false, false));
                }

                // Saturation (Helmet)
                int saturation = enchantmentManager.getLevel(player.getInventory().getHelmet(), EnchantmentType.SATURATION);
                if (saturation > 0 && player.getFoodLevel() < 20 && Math.random() < (0.15 * saturation)) {
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                    player.setSaturation(Math.min(20f, player.getSaturation() + 1.0f));
                }

                // Gears (Boots)
                int gears = enchantmentManager.getLevel(player.getInventory().getBoots(), EnchantmentType.GEARS);
                if (gears > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, gears - 1, true, false, false));
                }

                // Springs (Boots)
                int springs = enchantmentManager.getLevel(player.getInventory().getBoots(), EnchantmentType.SPRINGS);
                if (springs > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, springs - 1, true, false, false));
                }

                gameplayListener.applyAdditionalPassiveEffects(player);
            }
        }, 40L, 40L);

        getLogger().info("EaglerCustomEnchants enabled with " + EnchantmentType.values().length + " custom enchantments!");
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    @Override
    public void onDisable() {
        if (gameplayListener != null) {
            gameplayListener.restoreTemporaryLava();
        }
    }
}

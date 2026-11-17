package com.eagler.dynamiceco;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicEco extends JavaPlugin {
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        economyManager = new EconomyManager(this);

        MarketCommand commandHandler = new MarketCommand(economyManager);
        registerCommand("bal", commandHandler);
        registerCommand("pay", commandHandler);
        registerCommand("market", commandHandler);

        long stabilizeTicks = getConfig().getLong("stabilize-interval-seconds", 300L) * 20L;
        if (stabilizeTicks > 0L) {
            getServer().getScheduler().runTaskTimer(this, economyManager::stabilizeMarket,
                    stabilizeTicks, stabilizeTicks);
        }

        long saveTicks = getConfig().getLong("save-interval-seconds", 300L) * 20L;
        if (saveTicks > 0L) {
            getServer().getScheduler().runTaskTimer(this, economyManager::saveData, saveTicks, saveTicks);
        }

        getLogger().info("DynamicEco enabled.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.saveData();
        }
    }

    private void registerCommand(String name, MarketCommand handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Missing command declaration: " + name);
        }
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }
}

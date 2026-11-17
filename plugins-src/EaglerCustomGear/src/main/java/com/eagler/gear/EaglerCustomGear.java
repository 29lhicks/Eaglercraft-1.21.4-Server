package com.eagler.gear;

import org.bukkit.plugin.java.JavaPlugin;

public class EaglerCustomGear extends JavaPlugin {

    private GearManager gearManager;
    private RecipeGuiManager recipeGuiManager;
    private GearListener gearListener;

    @Override
    public void onEnable() {
        this.gearManager = new GearManager(this);
        this.recipeGuiManager = new RecipeGuiManager(this, gearManager);

        gearManager.registerCraftingRecipes();
        gearManager.startArmorSetTask();

        gearListener = new GearListener(this, gearManager, recipeGuiManager);
        getServer().getPluginManager().registerEvents(gearListener, this);

        GearCommand cmd = new GearCommand(gearManager);
        if (getCommand("cgear") != null) {
            getCommand("cgear").setExecutor(cmd);
            getCommand("cgear").setTabCompleter(cmd);
        }

        if (getCommand("recipes") != null) {
            getCommand("recipes").setExecutor((sender, command, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player player) {
                    recipeGuiManager.openRecipeMenu(player, 0);
                } else {
                    sender.sendMessage("Only players can view the 3x3 recipe GUI.");
                }
                return true;
            });
        }

        getLogger().info("EaglerCustomGear enabled with " + CustomGearType.values().length + " custom gear, tools, weapons, and health items!");
    }

    @Override
    public void onDisable() {
        if (gearListener != null) {
            gearListener.dropActiveBoomerangs();
        }
        if (gearManager != null) {
            getServer().getOnlinePlayers().forEach(gearManager::clearPlayerAttributes);
        }
    }

    public GearManager getGearManager() {
        return gearManager;
    }
}

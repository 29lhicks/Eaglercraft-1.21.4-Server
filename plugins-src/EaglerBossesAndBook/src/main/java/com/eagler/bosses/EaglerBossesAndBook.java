package com.eagler.bosses;

import org.bukkit.plugin.java.JavaPlugin;

public class EaglerBossesAndBook extends JavaPlugin {

    private ItemBridge itemBridge;
    private RecipeBookManager recipeBookManager;
    private BossManager bossManager;

    @Override
    public void onEnable() {
        this.itemBridge = new ItemBridge(this);
        this.recipeBookManager = new RecipeBookManager(this, itemBridge);
        this.bossManager = new BossManager(this, itemBridge);

        recipeBookManager.registerRecipes();
        bossManager.startAbilityTask();

        getServer().getPluginManager().registerEvents(new BossListener(this, itemBridge, recipeBookManager, bossManager), this);

        BossCommand cmd = new BossCommand(itemBridge, bossManager);
        if (getCommand("cboss") != null) {
            getCommand("cboss").setExecutor(cmd);
            getCommand("cboss").setTabCompleter(cmd);
        }
        if (getCommand("summonboss") != null) {
            getCommand("summonboss").setExecutor(cmd);
            getCommand("summonboss").setTabCompleter(cmd);
        }

        if (getCommand("recipebook") != null) {
            getCommand("recipebook").setExecutor((sender, command, label, args) -> {
                if (sender instanceof org.bukkit.entity.Player player) {
                    recipeBookManager.giveRecipeBook(player);
                } else {
                    sender.sendMessage("Only players can receive the Recipe Tome.");
                }
                return true;
            });
        }

        getLogger().info("EaglerBossesAndBook enabled with 4 Bosses, 4 Elite Mobs, 8 Custom Materials/Crystals, and /recipebook!");
    }

    @Override
    public void onDisable() {
        if (bossManager != null) {
            bossManager.cleanupAllBossBars();
        }
    }
}

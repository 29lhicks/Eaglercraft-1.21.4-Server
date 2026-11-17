package com.customrealms.relics;

import com.customrealms.relics.commands.RelicsCommand;
import com.customrealms.relics.items.CustomItemRegistry;
import com.customrealms.relics.items.RecipeManager;
import com.customrealms.relics.listeners.CombatAndBossListener;
import com.customrealms.relics.listeners.EverlastingBakedPotatoListener;
import com.customrealms.relics.listeners.PassiveBuffNerfTask;
import com.customrealms.relics.listeners.ToolAndInteractListener;
import com.customrealms.relics.network.BungeeNotifier;
import com.customrealms.relics.structures.StructureGeneratorListener;
import com.customrealms.relics.structures.VanillaStructureLootListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RelicsAndRuinsPlugin extends JavaPlugin {

    private static RelicsAndRuinsPlugin instance;
    private CustomItemRegistry itemRegistry;
    private BungeeNotifier bungeeNotifier;
    private PassiveBuffNerfTask passiveBuffNerfTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.bungeeNotifier = new BungeeNotifier(this);

        this.itemRegistry = new CustomItemRegistry(this);
        this.itemRegistry.registerAll();

        RecipeManager recipeManager = new RecipeManager(this, itemRegistry);
        recipeManager.registerRecipes();

        getServer().getPluginManager().registerEvents(new CombatAndBossListener(this, itemRegistry, bungeeNotifier), this);
        getServer().getPluginManager().registerEvents(new EverlastingBakedPotatoListener(itemRegistry), this);
        getServer().getPluginManager().registerEvents(new ToolAndInteractListener(this, itemRegistry), this);
        getServer().getPluginManager().registerEvents(new StructureGeneratorListener(this, itemRegistry), this);
        getServer().getPluginManager().registerEvents(new VanillaStructureLootListener(itemRegistry), this);

        passiveBuffNerfTask = new PassiveBuffNerfTask(this, itemRegistry);
        getServer().getPluginManager().registerEvents(passiveBuffNerfTask, this);
        passiveBuffNerfTask.runTaskTimer(this, 20L, 20L);

        if (getCommand("relics") != null) {
            RelicsCommand cmd = new RelicsCommand(this, itemRegistry);
            getCommand("relics").setExecutor(cmd);
            getCommand("relics").setTabCompleter(cmd);
        }

        getLogger().info("RelicsAndRuins 1.21.4 enabled! Registered " + itemRegistry.getAllItems().size() + " custom relics & worldgen structures.");
    }

    @Override
    public void onDisable() {
        if (passiveBuffNerfTask != null) {
            passiveBuffNerfTask.clearSprintPenalties();
        }
        getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
    }

    public static RelicsAndRuinsPlugin getInstance() {
        return instance;
    }

    public CustomItemRegistry getItemRegistry() {
        return itemRegistry;
    }
}

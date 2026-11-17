package com.eagler.structures;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class EaglerCustomStructures extends JavaPlugin {

    private LootGenerator lootGenerator;
    private StructureBuilder structureBuilder;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin customEnchantsPlugin = getServer().getPluginManager().getPlugin("EaglerCustomEnchants");
        Plugin customGearPlugin = getServer().getPluginManager().getPlugin("EaglerCustomGear");
        Plugin relicsPlugin = getServer().getPluginManager().getPlugin("RelicsAndRuins");
        if (customEnchantsPlugin != null) {
            getLogger().info("Hooked into EaglerCustomEnchants! Structures will generate with custom enchanted books and gear.");
        } else {
            getLogger().warning("EaglerCustomEnchants not found. Using direct PDC fallback compatible with EaglerCustomEnchants.");
        }
        if (customGearPlugin == null) {
            getLogger().warning("EaglerCustomGear not found. Structures will generate vanilla gear instead of custom gear.");
        }
        if (relicsPlugin == null) {
            getLogger().warning("RelicsAndRuins not found. Structures will not include its custom loot.");
        }

        this.lootGenerator = new LootGenerator(this, customEnchantsPlugin, customGearPlugin, relicsPlugin);
        this.structureBuilder = new StructureBuilder(lootGenerator);

        getServer().getPluginManager().registerEvents(new ChunkPopulateListener(this, structureBuilder), this);

        StructureCommand cmd = new StructureCommand(structureBuilder);
        if (getCommand("cstruct") != null) {
            getCommand("cstruct").setExecutor(cmd);
            getCommand("cstruct").setTabCompleter(cmd);
        }

        getLogger().info("EaglerCustomStructures enabled with " + StructureType.values().length + " structure types!");
    }
}
